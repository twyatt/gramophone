package com.traviswyatt.qd.features.dictate

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.traviswyatt.qd.AppTheme
import com.traviswyatt.qd.wifiIpAddress

class DictateScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { DictateScreenModel() }
        val isAvailable by screenModel.dictation.isAvailable.collectAsState()
        val isDictating by screenModel.dictation.isDictating.collectAsState(false)
        val transcript by screenModel.dictation.transcript.collectAsState()
        val ipAddress = remember { wifiIpAddress() }

        AppTheme {
            val border = if (isDictating) Color.Green else Color.Transparent
            Box(
                Modifier
                    .background(color = MaterialTheme.colors.background)
                    .border(15.dp, border)
                    .padding(15.dp)
                    .fillMaxSize()
                    .clickable {
                        screenModel.dictation.toggle()
                    }
            ) {
                val message = if (isAvailable) {
                    transcript
                } else {
                    "⚠️ Dictation unavailable."
                }
                Text(message, fontSize = 72.sp, lineHeight = 80.sp)
            }
        }
    }
}
