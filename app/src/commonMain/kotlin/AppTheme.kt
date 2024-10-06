package com.traviswyatt.qd

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.contentColorFor
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle

private val LightColors = lightColors()
private val DarkColors = darkColors()

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colors = if (darkTheme) DarkColors else LightColors,
    ) {
        ProvideTextStyle(
            TextStyle(color = contentColorFor(backgroundColor = MaterialTheme.colors.background)),
            content,
        )
    }
}
