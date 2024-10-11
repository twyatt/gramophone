package com.traviswyatt.qd

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

val settings = Settings(GlobalScope, appDataStore)

val client = combine(
    settings.isHost,
    settings.host,
) { isHost, host ->
    host.takeUnless { isHost }
}.filterNotNull()
    .map(::Client)
    .stateIn(GlobalScope, Eagerly, null)

fun init() {
    GlobalScope.clearTranscriptOnIdle()
}
