package com.traviswyatt.qd

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

private fun createAppDataStore(): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath { appDataStorePath.toPath() }

val appDataStore by lazy(::createAppDataStore)
