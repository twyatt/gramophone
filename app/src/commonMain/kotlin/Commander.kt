package com.traviswyatt.qd

class Commander(
    private val settings: Settings,
) {

    fun handle(text: String): Boolean {
        when (text.lowercase()) {
            "dictation disable send" -> setTransmit(false)
            "dictation enable send" -> setTransmit(true)
            else -> return false
        }
        return true
    }

    private fun setTransmit(enabled: Boolean) {
        settings.setTransmit(enabled)
    }
}
