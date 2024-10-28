package com.traviswyatt.gramophone

internal actual val appDataStorePath: String
    get() = applicationContext
        .filesDir
        .resolve(appDataStoreFilename)
        .absolutePath
