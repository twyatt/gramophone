package com.traviswyatt.qd

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface Dictation {
    val isAvailable: StateFlow<Boolean>
    val isDictating: Flow<Boolean>
    val transcript: StateFlow<String>
    fun start()
    fun toggle()
    fun cancel()
}

expect fun CoroutineScope.Dictation(): Dictation
