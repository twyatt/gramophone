package com.traviswyatt.qd

import com.juul.khronicle.Log
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout

private const val SearchConcurrency = 10
private val IndentTimeout = 15.seconds

class HostFinder {

    private val dispatcher = Dispatchers.IO
    private val semaphore = Semaphore(SearchConcurrency)

    val isSearching = MutableStateFlow(false)

    suspend fun find() = coroutineScope {
        isSearching.value = true

        val ip = requireNotNull(wifiIpAddress)
        val prefix = ip.substringBeforeLast('.')
        val mine = ip.substringAfterLast('.').toInt()

        val found = CompletableDeferred<String>()

        val jobs = (2..254).filterNot { it == mine }.map {
            launch(dispatcher) {
                semaphore.withPermit {
                    val address = "${prefix}.$it"
                    if (isReachable(address)) found.complete(address)
                }
            }
        }

        try {
            select<String?> {
                found.onAwait { result ->
                    jobs.forEach { it.cancel() }
                    result
                }
                async { jobs.joinAll() }.onAwait {
                    Log.warn { "No host found" }
                    null
                }
            }
        } finally {
            isSearching.value = false
        }
    }
}

suspend fun isReachable(address: String): Boolean {
    val client = Client(address)
    Log.verbose { "🔍 $address" }
    return try {
        withTimeout(IndentTimeout) {
            client.ident().also { response ->
                Log.debug { "🎉 $address: $response" }
            }
        } == SERVER_IDENT
    } catch (e: Exception) {
        val status = when (e) {
            is CancellationException -> "🗑️"
            else -> "❌"
        }
        Log.warn { "$status $address" }
        false
    } finally {
        client.cancel()
    }
}


