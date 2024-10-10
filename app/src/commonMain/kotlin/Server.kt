package com.traviswyatt.qd

import kotlinx.coroutines.flow.MutableStateFlow

interface Server {
    fun start()
}

expect fun Server(transcript: MutableStateFlow<String>): Server
