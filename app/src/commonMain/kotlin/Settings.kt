package com.traviswyatt.gramophone

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.juul.khronicle.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val FontSizePreferenceKey = floatPreferencesKey("font_size")
private val HostPreferenceKey = stringPreferencesKey("host")
private val IsHostPreferenceKey = booleanPreferencesKey("is_host")
private val ShowInstructionsKey = booleanPreferencesKey("show_instructions")

class Settings(
    private val scope: CoroutineScope,
    private val dataStore: DataStore<Preferences>,
) {

    val isHost =
        dataStore.data
            .map { it.getOrDefault(IsHostPreferenceKey, isTablet) }
            .distinctUntilChanged()
            .stateIn(scope, Eagerly, isTablet)

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

    val host = dataStore.data.map { it[HostPreferenceKey] }

    val showInstructions = dataStore.data
        .map { it.getOrDefault(ShowInstructionsKey, true) }
        .distinctUntilChanged()
        .stateIn(scope, Eagerly, true)

    fun hideInstructions() {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[ShowInstructionsKey] = false
            }
            Log.debug { "Hid instructions" }
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

    fun reset() {
        scope.launch {
            dataStore.edit { preferences ->
                preferences.clear()
            }
            transcript.value = "Reset complete"
        }
    }
}
