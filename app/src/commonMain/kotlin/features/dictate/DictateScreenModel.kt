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
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val FontSizeRange = 6f..240f
private const val DefaultFontSize = 96f // sp
private val FontSizePreferenceKey = floatPreferencesKey("font_size")

class DictateScreenModel : ScreenModel {

    private val dataStore = createAppDataStore()
    val dictation = screenModelScope.Dictation()
    private val client = MutableStateFlow<Client?>(null)

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
