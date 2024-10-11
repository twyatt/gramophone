package com.traviswyatt.qd

interface Server {
    fun start()
    fun stop()
}

expect fun Server(): Server
