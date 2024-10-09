package com.traviswyatt.qd

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NettyServer : Server {

    private val _incoming = MutableStateFlow<String?>(null)
    override val incoming = _incoming.asStateFlow()
    private val http = embeddedServer(Netty, port = 8080) {
        routing {
            post("/") {
                _incoming.value = call.receiveText()
                call.respondText("OK")
            }

            delete("/") {
                clear()
                call.respondText("OK")
            }
        }
    }

    override fun clear() {
        _incoming.value = null
    }

    override fun start() {
        http.start(wait = false)
    }
}
