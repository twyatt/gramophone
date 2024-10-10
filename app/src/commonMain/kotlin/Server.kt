package com.traviswyatt.qd

val SERVER_IDENT = "gramophone"

interface Server {
    fun start()
}

expect fun Server(): Server
