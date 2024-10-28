package com.traviswyatt.gramophone

import android.content.Context
import androidx.startup.Initializer

internal lateinit var applicationContext: Context
    private set

public object Gramophone

public class GramophoneInitializer : Initializer<Gramophone> {

    override fun create(context: Context): Gramophone {
        applicationContext = context.applicationContext
        return Gramophone
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
