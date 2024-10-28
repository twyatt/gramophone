package com.traviswyatt.gramophone

import androidx.datastore.preferences.core.Preferences

fun <T> Preferences.getOrDefault(
    key: Preferences.Key<T>,
    default: T,
): T = get(key) ?: default
