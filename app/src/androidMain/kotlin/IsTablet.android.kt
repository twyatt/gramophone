package com.traviswyatt.qd

import android.content.res.Configuration

actual val isTablet by lazy {
    (applicationContext.resources.configuration.screenLayout
            and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
}
