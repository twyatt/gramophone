package com.traviswyatt.qd.features.dictate

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.juul.khronicle.Log
import com.traviswyatt.qd.Client
import com.traviswyatt.qd.Dictation
import com.traviswyatt.qd.server
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class DictateScreenModel : ScreenModel {

    val dictation = screenModelScope.Dictation()

    private val client = MutableStateFlow<Client?>(null)

    private val _showIpDialog = MutableStateFlow(false)
    val showIpDialog = _showIpDialog.asStateFlow()

    init {
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

    override fun onDispose() {
        dictation.cancel()
    }

    fun setIp() {
        _showIpDialog.value = true
    }
}
