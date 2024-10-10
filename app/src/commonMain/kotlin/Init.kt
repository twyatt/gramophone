package com.traviswyatt.qd

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
    GlobalScope.launch {
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
}
