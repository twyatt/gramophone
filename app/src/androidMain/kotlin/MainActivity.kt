package com.traviswyatt.qd

import android.os.Bundle
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.juul.khronicle.Log
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private val isAppBeingUsed = MutableStateFlow(false)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureLogging()
        init()

        transcript
            .onEach { isAppBeingUsed.value = true }
            .debounce(10.minutes)
            .onEach { isAppBeingUsed.value = false }
            .launchIn(GlobalScope)

        combine(
            isCharging,
            isAppBeingUsed,
        ) { charging, active -> charging || active }
            .distinctUntilChanged()
            .onEach { shouldKeepOn ->
                if (shouldKeepOn) {
                    Log.debug { "Keeping screen on" }
                    window.addFlags(FLAG_KEEP_SCREEN_ON)
                } else {
                    Log.debug { "Allowing screen to sleep" }
                    window.clearFlags(FLAG_KEEP_SCREEN_ON)
                }
            }
            .flowOn(Dispatchers.Main)
            .launchIn(GlobalScope)

        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            App()
        }
    }
}
