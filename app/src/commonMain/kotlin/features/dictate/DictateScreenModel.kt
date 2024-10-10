package com.traviswyatt.qd.features.dictate

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.juul.khronicle.Log
import com.traviswyatt.qd.Client
import com.traviswyatt.qd.Dictation
import com.traviswyatt.qd.createAppDataStore
import com.traviswyatt.qd.server
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val FontSizeRange = 6f..240f
private const val DefaultFontSize = 96f // sp
private val FontSizePreferenceKey = floatPreferencesKey("font_size")

class DictateScreenModel(val permissionsController: PermissionsController) : ScreenModel {

    private val dataStore = createAppDataStore()
    val dictation = screenModelScope.Dictation()
    private val client = MutableStateFlow<Client?>(null)

    val permissionState = MutableStateFlow(NotDetermined)

    private val _showIpDialog = MutableStateFlow(false)
    val showIpDialog = _showIpDialog.asStateFlow()

    private val _fontSize = MutableStateFlow(DefaultFontSize)
    val fontSize = _fontSize.asStateFlow()

    init {
        screenModelScope.launch {
            dataStore.data.first()[FontSizePreferenceKey]?.let { fontSize ->
                _fontSize.value = fontSize
                Log.debug {
                    "Restored font size: $fontSize"
                }
            }

            fontSize.debounce(1.seconds).collect { fontSize ->
                dataStore.edit { preferences ->
                    preferences[FontSizePreferenceKey] = fontSize
                }
                Log.debug {
                    "Saved font size: $fontSize"
                }
            }
        }

        dictation.isDictating
            .filter { it }
            .onEach {
                client.value?.clear()
            }
            .launchIn(screenModelScope)

        dictation.transcript.onEach { text ->
            try {
                client.value?.send(text)
            } catch (e: Exception) {
                Log.error(e) { "Failed to send: $text" }
            }
        }.launchIn(screenModelScope)
    }

    val transcript = combine(
        dictation.transcript,
        server.incoming,
    ) { dictation, received ->
        received ?: dictation
    }

    private val isRequestingRecordPermission = MutableStateFlow<Boolean?>(null)

    /**
     * Re-check permissions when screen resumes (can occur after coming back from app settings
     * screen where user may have granted needed permissions).
     */
    fun onResumed() {
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
    }

    fun openAppSettings() {
        permissionsController.openAppSettings()
    }

    fun toggleDictation() {
        server.clear()
        screenModelScope.launch {
            if (dictation.isDictating.first()) {
                dictation.cancel()
            } else {
                startDictation()
            }
        }
    }

    private suspend fun startDictation() {
        if (permissionState.value == Granted) {
            dictation.start()
        } else {
            requestAndUpdateRecordPermission()
            if (permissionState.value == Granted) {
                dictation.start()
            }
        }
    }

    private suspend fun requestAndUpdateRecordPermission() {
        // Once we've been granted permission we no longer need to request permission. Apple and
        // Android will kill the app if permissions are revoked.
        if (permissionState.value == Granted) return

        isRequestingRecordPermission.value = true
        permissionState.value = permissionsController.requestPermission(RECORD_AUDIO)
    }

    fun setServer(ipLastDigit: String) {
        if (ipLastDigit.isNotEmpty()) {
            client.value = Client("10.0.0.$ipLastDigit")
        }
        _showIpDialog.value = false
    }

    fun onZoomChange(zoomChange: Float) {
        _fontSize.update { previous ->
            (previous * zoomChange).coerceIn(FontSizeRange)
        }
    }

    override fun onDispose() {
        dictation.cancel()
    }

    fun setIp() {
        _showIpDialog.value = true
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
