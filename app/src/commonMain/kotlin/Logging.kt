package com.traviswyatt.qd

import com.juul.khronicle.ConsoleLogger
import com.juul.khronicle.ConstantTagGenerator
import com.juul.khronicle.Log

fun configureLogging() {
    Log.tagGenerator = ConstantTagGenerator(tag = "QuickDictate")
    Log.dispatcher.install(ConsoleLogger)
}
