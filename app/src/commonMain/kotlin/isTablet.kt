package com.traviswyatt.qd

expect val isTablet: Boolean

val isPhone: Boolean
    get() = !isTablet
