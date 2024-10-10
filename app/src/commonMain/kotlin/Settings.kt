package com.traviswyatt.qd

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import com.juul.khronicle.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val FontSizePreferenceKey = floatPreferencesKey("font_size")
private val PreferenceTransmitKey = booleanPreferencesKey("transmit")

class Settings(
    private val scope: CoroutineScope,
    private val dataStore: DataStore<Preferences>,
) {

    fun setTransmit(enabled: Boolean) {
        Log.info { "Set transmit: $enabled" }
        scope.launch {
            dataStore.edit { preferences ->
                preferences[PreferenceTransmitKey] = enabled
            }
        }
    }

    suspend fun getFontSize(): Float? =
        dataStore.data.first()[FontSizePreferenceKey]?.also { fontSize ->
            Log.debug {
                "Restored font size: $fontSize"
            }
        }

    fun saveFontSize(fontSize: Float) {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[FontSizePreferenceKey] = fontSize
            }
            Log.debug {
                "Saved font size: $fontSize"
            }
        }
    }
}
