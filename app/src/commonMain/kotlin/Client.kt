package com.traviswyatt.gramophone

import com.juul.khronicle.Log
import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.delete
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.URLProtocol.Companion.HTTP
import kotlinx.coroutines.cancel

class Client(remoteHost: String) {

    private val http = HttpClient {
        defaultRequest {
            url {
                protocol = HTTP
                host = remoteHost
                port = 8080
            }
        }
    }

    suspend fun send(text: String) {
        try {
            http.put {
                setBody(text)
            }
        } catch (e: Exception) {
            Log.error(e) { "Failed to send: $text" }
        }
    }

    suspend fun clear() {
        try {
            http.delete {}
        } catch (e: Exception) {
            Log.error(e) { "Failed to clear" }
        }
    }

    fun cancel() {
        http.cancel()
    }
}
