package com.traviswyatt.qd

import kotlinx.coroutines.flow.StateFlow

interface Server {
    val incoming: StateFlow<String?>
    fun clear()
    fun start()
}

expect fun Server(): Server
