package com.traviswyatt.qd

internal actual val appDataStorePath: String
    get() = applicationContext
        .filesDir
        .resolve(appDataStoreFilename)
        .absolutePath
