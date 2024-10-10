package com.traviswyatt.qd

import kotlinx.coroutines.flow.MutableStateFlow

actual fun Server(transcript: MutableStateFlow<String>): Server = NopServer
