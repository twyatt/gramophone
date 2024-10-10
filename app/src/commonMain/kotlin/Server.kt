package com.traviswyatt.qd

interface Server {
    fun start()
}

expect fun Server(): Server
