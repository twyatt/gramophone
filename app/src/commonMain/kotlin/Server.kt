package com.traviswyatt.gramophone

interface Server {
    fun start()
    fun stop()
}

expect fun Server(): Server
