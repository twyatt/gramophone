package com.traviswyatt.qd

import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
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
        http.put {
            setBody(text)
        }
    }

    suspend fun clear() {
        http.delete {}
    }

    suspend fun ident(): String =
        http.get("/ident").bodyAsText()

    fun cancel() {
        http.cancel()
    }
}
