package com.traviswyatt.qd

import com.benasher44.uuid.uuidFrom
import com.juul.kable.Scanner
import com.juul.khronicle.Log
import com.traviswyatt.qd.bluetooth.ServiceUuid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

private const val TAG = "HostFinder"

object HostFinder {

    val isRunning = MutableStateFlow(false)

    private val scanner by lazy {
        Scanner {}
    }

    @OptIn(ExperimentalStdlibApi::class)
    suspend fun run() {
        isRunning.value = true
        Log.debug(tag = TAG) { "Starting scan" }
        try {
            val ip = scanner
                .advertisements
                .onEach { println("ADV: $it") }
                .map { it.serviceData(uuidFrom(ServiceUuid)) }
                .filterNotNull()
                .first()

            Log.debug(tag = TAG) {
                "Found advertisement with data: ${ip.toHexString()}"
            }
            val host = ip.map { it.toUByte().toInt() }.joinToString(".")
            Log.info(tag = TAG) { "Found host: $host" }
            settings.setHost(host)
        } finally {
            isRunning.value = false
        }
    }
}
