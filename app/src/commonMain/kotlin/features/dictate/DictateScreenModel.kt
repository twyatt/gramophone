package com.traviswyatt.qd.features.dictate

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.juul.khronicle.Log
import com.traviswyatt.qd.Commander
import com.traviswyatt.qd.Dictation
import com.traviswyatt.qd.Settings
import com.traviswyatt.qd.appDataStore
import com.traviswyatt.qd.client
import com.traviswyatt.qd.transcript
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.Permission.BLUETOOTH_ADVERTISE
import dev.icerock.moko.permissions.Permission.RECORD_AUDIO
import dev.icerock.moko.permissions.PermissionState.Denied
import dev.icerock.moko.permissions.PermissionState.DeniedAlways
import dev.icerock.moko.permissions.PermissionState.Granted
import dev.icerock.moko.permissions.PermissionState.NotDetermined
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val FontSizeRange = 6f..240f
private const val DefaultFontSize = 96f // sp

class DictateScreenModel(val permissionsController: PermissionsController) : ScreenModel {

    private val settings = Settings(GlobalScope, appDataStore)
    val dictation = screenModelScope.Dictation(Commander(settings))

    val recordPermissionState = MutableStateFlow(NotDetermined)
    val bluetoothPermissionState = MutableStateFlow(NotDetermined)

    private val _fontSize = MutableStateFlow(DefaultFontSize)
    val fontSize = _fontSize.asStateFlow()

    init {
        screenModelScope.launch {
            settings.getFontSize()?.let { fontSize ->
                _fontSize.value = fontSize
            }

            fontSize.debounce(1.seconds).collect { fontSize ->
                settings.saveFontSize(fontSize)
            }
        }

        dictation.isDictating
            .filter { it }
            .onEach {
                try {
                    client.value?.clear()
                } catch (e: Exception) {
                    Log.error(e) { "Failed to clear" }
                }
            }
            .launchIn(screenModelScope)

        transcript.onEach { text ->
            try {
                client.value?.send(text)
            } catch (e: Exception) {
                Log.error(e) { "Failed to send: $text" }
            }
        }.launchIn(screenModelScope)
    }

    private val isRequestingRecordPermission = MutableStateFlow<Boolean?>(null)
    private val isRequestingBluetoothPermission = MutableStateFlow<Boolean?>(null)

    /**
     * Re-check permissions when screen resumes (can occur after coming back from app settings
     * screen where user may have granted needed permissions).
     */
    fun onResumed() {
        transcript.compareAndSet("", "Welcome back!")

        screenModelScope.launch {
            when (isRequestingRecordPermission.value) {
                // After requesting permission, onResume is triggered, so we reset the
                // "is requesting" state here.
                true -> isRequestingRecordPermission.value = false

                false -> requestAndUpdateRecordPermission()

                // On Apple (until authorized) even checking the permission state will show a
                // permission dialog. We guard against showing the dialog prior to actually wanting
                // to request the permission by only checking permission after we've explicitly
                // requested permission.
                null -> {} // No-op
            }
        }

        screenModelScope.launch {
            when (isRequestingBluetoothPermission.value) {
                // After requesting permission, onResume is triggered, so we reset the
                // "is requesting" state here.
                true -> isRequestingBluetoothPermission.value = false

                false -> requestAndUpdateBluetoothPermission()

                // On Apple (until authorized) even checking the permission state will show a
                // permission dialog. We guard against showing the dialog prior to actually wanting
                // to request the permission by only checking permission after we've explicitly
                // requested permission.
                null -> {} // No-op
            }
        }
    }

    fun openAppSettings() {
        permissionsController.openAppSettings()
    }

    fun toggleDictation() {
        screenModelScope.launch {
            if (dictation.isDictating.first()) {
                dictation.cancel()
            } else {
                startDictation()
            }
        }
    }

    private suspend fun startDictation() {
        transcript.value = ""
        if (recordPermissionState.value == Granted) {
            dictation.start()
        } else {
            requestAndUpdateRecordPermission()
            if (recordPermissionState.value == Granted) {
                dictation.start()
            }
        }
    }

    private suspend fun requestAndUpdateRecordPermission() {
        // Once we've been granted permission we no longer need to request permission. Apple and
        // Android will kill the app if permissions are revoked.
        if (recordPermissionState.value == Granted) return

        isRequestingRecordPermission.value = true
        recordPermissionState.value = permissionsController.requestPermission(RECORD_AUDIO)
    }

    suspend fun requestAndUpdateBluetoothPermission() {
        // Once we've been granted permission we no longer need to request permission. Apple and
        // Android will kill the app if permissions are revoked.
        if (bluetoothPermissionState.value == Granted) return

        isRequestingBluetoothPermission.value = true
        bluetoothPermissionState.value = permissionsController.requestPermission(BLUETOOTH_ADVERTISE)
    }

    fun onZoomChange(zoomChange: Float) {
        _fontSize.update { previous ->
            (previous * zoomChange).coerceIn(FontSizeRange)
        }
    }

    override fun onDispose() {
        dictation.cancel()
    }
}

private suspend fun PermissionsController.requestPermission(permission: Permission) = try {
    providePermission(permission)
    Granted
} catch (e: Exception) {
    when (e) {
        is DeniedAlwaysException -> DeniedAlways
        is DeniedException, is RequestCanceledException -> Denied
        else -> throw e
    }
}
