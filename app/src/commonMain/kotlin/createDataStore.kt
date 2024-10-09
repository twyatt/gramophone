package com.traviswyatt.qd

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

fun createAppDataStore(): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath { appDataStorePath.toPath() }
