package com.traviswyatt.qd

import kotlinx.coroutines.CoroutineScope

actual fun CoroutineScope.Dictation(commander: Commander): Dictation = AndroidDictation(commander)
