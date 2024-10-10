package com.traviswyatt.qd

import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.delete
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.URLProtocol.Companion.HTTP
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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

    fun send(text: String) {
        GlobalScope.launch {
            http.put {
                setBody(text)
            }
        }
    }

    fun clear() {
        GlobalScope.launch {
            http.delete {}
        }
    }
}
