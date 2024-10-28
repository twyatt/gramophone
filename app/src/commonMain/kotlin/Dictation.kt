package com.traviswyatt.gramophone

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface Dictation {
    val isAvailable: StateFlow<Boolean>
    val isDictating: Flow<Boolean>
    fun start()
    fun toggle()
    fun cancel()
}

expect fun CoroutineScope.Dictation(commander: Commander): Dictation
