package com.traviswyatt.qd

import com.juul.khronicle.Log
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

val hostFinder = HostFinder()

val settings = Settings(GlobalScope, appDataStore)

val client = settings.host
    .map { host ->
        if (host == null || !isReachable(host)) {
            null
        } else {
            Client(host)
        }
    }.stateIn(GlobalScope, Eagerly, null)

fun init() {
    GlobalScope.configureHost()
    GlobalScope.clearTranscriptOnIdle()
}

private fun CoroutineScope.configureHost() = launch {
    if (settings.getTransmit()) {
        val host = settings.getHost()
        if (host == null || !isReachable(host)) {
            launch {
                val job = currentCoroutineContext().job

                launch {
                    settings.transmit.filterNot { it }.first()
                    job.cancel()
                }

                launch {
                    hostFinder.find()?.let { ip ->
                        settings.setHost(ip)
                    }
                    job.cancel()
                }
            }
        }
    }
}

private fun CoroutineScope.clearTranscriptOnIdle() {
    transcript
        .filterNot { it.isNullOrEmpty() }
        .debounce(2.minutes)
        .onEach {
            if (!settings.getTransmit()) {
                Log.debug { "Clearing transcript" }
                transcript.value = ""
            }
        }
        .launchIn(this)
}
