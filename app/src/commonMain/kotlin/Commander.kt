package com.traviswyatt.qd

import com.juul.khronicle.Log

class Commander(
    private val settings: Settings,
) {

    fun handle(text: String): Boolean {
        when (text.lowercase().command) {
            "disable send" -> setTransmit(false)
            "enable send" -> setTransmit(true)
            else -> return false
        }
        Log.info { "Handled command: $text" }
        return true
    }

    private fun setTransmit(enabled: Boolean) {
        settings.setTransmit(enabled)
    }
}

private val String.command: String?
    get() = when {
        startsWith("grandma phone") -> substringAfter("grandma phone").trimStart()
        startsWith("gramophone") -> substringAfter("gramophone").trimStart()
        else -> null
    }
