package com.traviswyatt.qd

import kotlinx.coroutines.CoroutineScope

actual fun CoroutineScope.Dictation(commander: Commander): Dictation = AppleDictation(this, commander)
