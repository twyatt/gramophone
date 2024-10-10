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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val FontSizePreferenceKey = floatPreferencesKey("font_size")
private val TransmitPreferenceKey = booleanPreferencesKey("transmit")
private val HostPreferenceKey = stringPreferencesKey("host")

private fun <T> Preferences.getOrDefault(key: Preferences.Key<T>, default: T): T =
    get(key) ?: default

class Settings(
    private val scope: CoroutineScope,
    private val dataStore: DataStore<Preferences>,
) {

    val transmit: Flow<Boolean> =
        dataStore.data.map { it.getOrDefault(TransmitPreferenceKey, isPhone) }

    suspend fun getTransmit(): Boolean =
        dataStore.data.first().getOrDefault(TransmitPreferenceKey, isPhone).also { transmit ->
            Log.debug {
                "Restored transmit: $transmit"
            }
        }

    fun setTransmit(enabled: Boolean) {
        Log.info { "Set transmit: $enabled" }
        scope.launch {
            dataStore.edit { preferences ->
                preferences[TransmitPreferenceKey] = enabled
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

    val host: Flow<String?> = dataStore.data.map { it[HostPreferenceKey] }

    suspend fun getHost(): String? =
        dataStore.data.first()[HostPreferenceKey]?.also { host ->
            Log.debug {
                "Restored host: $host"
            }
        }

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
}