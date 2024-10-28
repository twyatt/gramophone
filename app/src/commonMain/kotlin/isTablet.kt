package com.traviswyatt.gramophone

expect val isTablet: Boolean

val isPhone: Boolean
    get() = !isTablet
