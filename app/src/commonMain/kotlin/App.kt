package com.traviswyatt.gramophone

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import com.traviswyatt.gramophone.features.dictate.DictateScreen
import kotlinx.coroutines.flow.MutableStateFlow

val transcript = MutableStateFlow("")

@Composable
fun App() {
    MaterialTheme {
        Navigator(DictateScreen())
    }
}
