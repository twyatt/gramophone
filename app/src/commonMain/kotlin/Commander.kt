package com.traviswyatt.qd

import com.juul.khronicle.Log

class Commander(
    private val settings: Settings,
) {

    fun handle(text: String): Boolean {
        when (text.lowercase().command) {
            "host" -> setIsHost(true)
            "client" -> setIsHost(false)
            "reset" -> reset()
            else -> return false
        }
        Log.info { "Handled command: $text" }
        return true
    }

    private fun reset() {
        settings.reset()
    }

    private fun setIsHost(enabled: Boolean) {
        settings.setIsHost(enabled)
    }
}

private val String.command: String?
    get() = when {
        startsWith("grandma phone") -> substringAfter("grandma phone").trimStart()
        startsWith("gramophone") -> substringAfter("gramophone").trimStart()
        else -> null
    }
