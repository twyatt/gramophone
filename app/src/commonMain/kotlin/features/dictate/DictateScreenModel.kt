package com.traviswyatt.gramophone.features.dictate

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.traviswyatt.gramophone.Commander
import com.traviswyatt.gramophone.Dictation
import com.traviswyatt.gramophone.HostFinder
import com.traviswyatt.gramophone.client
import com.traviswyatt.gramophone.settings
import com.traviswyatt.gramophone.transcript
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.Permission.BLUETOOTH_SCAN
import dev.icerock.moko.permissions.Permission.RECORD_AUDIO
import dev.icerock.moko.permissions.PermissionState.Denied
import dev.icerock.moko.permissions.PermissionState.DeniedAlways
import dev.icerock.moko.permissions.PermissionState.Granted
import dev.icerock.moko.permissions.PermissionState.NotDetermined
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val FontSizeRange = 6f..240f
private const val DefaultFontSize = 96f // sp

class DictateScreenModel(val permissionsController: PermissionsController) : ScreenModel {

    val dictation = screenModelScope.Dictation(Commander(settings))

    val recordPermissionState = MutableStateFlow(NotDetermined)
    val bluetoothPermissionState = MutableStateFlow(NotDetermined)

    private val _fontSize = MutableStateFlow(DefaultFontSize)
    val fontSize = _fontSize.asStateFlow()

    private val transmissionEnabled = MutableStateFlow(true)

    val needsHost = combine(
        settings.isHost.map { !it },
        settings.host,
        transmissionEnabled,
    ) { isClient, host, transmissionEnabled -> isClient && host == null && transmissionEnabled }
        .distinctUntilChanged()

    init {
        screenModelScope.launch {
            settings.fontSize.first()?.let { fontSize ->
                _fontSize.value = fontSize
            }

            fontSize.debounce(1.seconds).collect { fontSize ->
                settings.saveFontSize(fontSize)
            }
        }

        dictation.isDictating
            .filter { it }
            .onEach { client.value?.clear() }
            .launchIn(screenModelScope)

        transcript.onEach { text ->
            client.value?.send(text)
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

    fun disableTransmission() {
        transmissionEnabled.value = false
    }

    fun findHost() {
        screenModelScope.launch {
            if (bluetoothPermissionState.value == Granted) {
                HostFinder.run()
            } else {
                requestAndUpdateBluetoothPermission()
                if (bluetoothPermissionState.value == Granted) {
                    HostFinder.run()
                }
            }
        }
    }

    private suspend fun requestAndUpdateBluetoothPermission() {
        // Once we've been granted permission we no longer need to request permission. Apple and
        // Android will kill the app if permissions are revoked.
        if (bluetoothPermissionState.value == Granted) return

        isRequestingBluetoothPermission.value = true
        bluetoothPermissionState.value = permissionsController.requestPermission(BLUETOOTH_SCAN)
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
