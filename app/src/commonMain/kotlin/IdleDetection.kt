package com.traviswyatt.qd

import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

val isAppBeingUsed = MutableStateFlow(false)

fun CoroutineScope.configureIdleDetection() {
    transcript
        .onEach { isAppBeingUsed.value = true }
        .debounce(10.minutes)
        .onEach { isAppBeingUsed.value = false }
        .launchIn(this)
}
