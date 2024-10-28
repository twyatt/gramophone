package com.traviswyatt.gramophone

import com.juul.khronicle.ConsoleLogger
import com.juul.khronicle.ConstantTagGenerator
import com.juul.khronicle.Log

fun configureLogging() {
    Log.tagGenerator = ConstantTagGenerator(tag = "Gramophone")
    Log.dispatcher.install(ConsoleLogger)
}
