package com.traviswyatt.gramophone

import kotlinx.coroutines.CoroutineScope

actual fun CoroutineScope.Dictation(commander: Commander): Dictation = AndroidDictation(commander)
