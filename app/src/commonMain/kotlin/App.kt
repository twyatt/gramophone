package com.traviswyatt.qd

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import com.traviswyatt.qd.features.dictate.DictateScreen

@Composable
fun App() {
    MaterialTheme {
        Navigator(DictateScreen())
    }
}
