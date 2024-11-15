package com.traviswyatt.gramophone

import com.juul.khronicle.Log
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.ApplicationEngineFactory
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.routing

private const val TAG = "KtorServer"
private const val SERVER_IDENT = "gramophone"

class KtorServer<TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration>(
    engine: ApplicationEngineFactory<TEngine, TConfiguration>,
) : Server {

    private val server = embeddedServer(engine, port = 8080) {
        routing {
            get("/ident") {
                call.respondText(SERVER_IDENT)
            }

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
        Log.info(tag = TAG) { "Starting" }
        server.start(wait = false)
    }

    override fun stop() {
        Log.info(tag = TAG) { "Stopping" }
        server.stop()
    }
}
