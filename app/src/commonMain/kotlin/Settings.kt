package com.traviswyatt.qd

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.juul.khronicle.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val FontSizePreferenceKey = floatPreferencesKey("font_size")
private val HostPreferenceKey = stringPreferencesKey("host")
private val IsHostPreferenceKey = booleanPreferencesKey("is_host")
private val TransmitPreferenceKey = booleanPreferencesKey("transmit")

class Settings(
    private val scope: CoroutineScope,
    private val dataStore: DataStore<Preferences>,
) {

    val isHost: Flow<Boolean> =
        dataStore.data.map { it.getOrDefault(IsHostPreferenceKey, isTablet) }
            .distinctUntilChanged()

    val fontSize = dataStore.data.map { it[FontSizePreferenceKey] }

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

    val host: Flow<String?> = dataStore.data.map { it[HostPreferenceKey] }

    fun setHost(host: String) {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[HostPreferenceKey] = host
            }
            Log.debug {
                "Saved host: $host"
            }
        }
    }

    fun setIsHost(isHost: Boolean) {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[IsHostPreferenceKey] = isHost
            }
            Log.debug {
                "Saved isHost: $isHost"
            }
        }
    }
}
