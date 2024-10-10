package com.traviswyatt.qd

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import kotlinx.coroutines.flow.MutableStateFlow

class NettyServer(private val transcript: MutableStateFlow<String>) : Server {

    private val http = embeddedServer(Netty, port = 8080) {
        routing {
            put("/") {
                transcript.value = call.receiveText()
                call.respondText("OK")
            }

            delete("/") {
                transcript.value = ""
                call.respondText("OK")
            }
        }
    }

    override fun start() {
        http.start(wait = false)
    }
}
