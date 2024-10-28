package com.traviswyatt.gramophone

import com.juul.khronicle.Log
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

fun CoroutineScope.clearTranscriptOnIdle() {
    transcript
        .filterNot(String::isNullOrEmpty)
        .debounce(2.minutes)
        .onEach {
            if (settings.isHost.first()) {
                Log.debug { "Clearing transcript" }
                transcript.value = ""
            }
        }
        .launchIn(this)
}
