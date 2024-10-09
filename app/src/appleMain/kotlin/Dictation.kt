package com.traviswyatt.qd

import kotlinx.coroutines.CoroutineScope

actual fun CoroutineScope.Dictation(): Dictation = AppleDictation(this)
