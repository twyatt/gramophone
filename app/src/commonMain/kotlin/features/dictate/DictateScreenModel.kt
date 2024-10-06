package com.traviswyatt.qd.features.dictate

import cafe.adriel.voyager.core.model.ScreenModel
import com.traviswyatt.qd.Dictation

class DictateScreenModel : ScreenModel {

    val dictation = Dictation()

    override fun onDispose() {
        dictation.cancel()
    }
}