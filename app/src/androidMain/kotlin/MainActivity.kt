package com.traviswyatt.qd

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.juul.khronicle.Log
import com.traviswyatt.qd.bluetooth.Advertiser
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private val isAppBeingUsed = MutableStateFlow(false)
val isAppForegrounded = MutableStateFlow(true)

class MainActivity : ComponentActivity() {

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        monitorLifecycle()
        configureLogging()
        GlobalScope.configureServer()
        GlobalScope.configureAdvertising()
        init()

        transcript
            .onEach { isAppBeingUsed.value = true }
            .debounce(10.minutes)
            .onEach { isAppBeingUsed.value = false }
            .launchIn(GlobalScope)

        combine(
            isCharging,
            isAppForegrounded,
            isAppBeingUsed,
        ) { charging, foregrounded, active -> (charging || active) && foregrounded }
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

private fun CoroutineScope.configureServer() {
    var server: Server? = null
    settings.isHost
        .onEach { host ->
            when (host) {
                true -> server = Server().apply(Server::start)
                false -> server?.stop()
            }
        }
        .launchIn(this)
}

@SuppressLint("MissingPermission") // fixme: Check for permission.
private fun CoroutineScope.configureAdvertising() = launch {
    combine(
        isAppForegrounded,
        settings.isHost,
        hostAddress,
    ) { foregrounded, host, address ->
        address.takeIf { foregrounded && host }
    }.distinctUntilChanged()
        .collectLatest { address ->
            address?.let { Advertiser.advertise(it) }
        }
}
