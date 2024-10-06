package com.traviswyatt.qd

import android.content.Context
import androidx.startup.Initializer

internal lateinit var applicationContext: Context
    private set

public object QuickDictate

public class QdInitializer : Initializer<QuickDictate> {

    override fun create(context: Context): QuickDictate {
        applicationContext = context.applicationContext
        return QuickDictate
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
