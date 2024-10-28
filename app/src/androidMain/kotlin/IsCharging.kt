package com.traviswyatt.gramophone

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.BatteryManager.BATTERY_STATUS_CHARGING
import android.os.BatteryManager.BATTERY_STATUS_FULL
import com.juul.tuulbox.coroutines.flow.broadcastReceiverFlow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

val isCharging = broadcastReceiverFlow(IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    .map { intent ->
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        status == BATTERY_STATUS_CHARGING || status == BATTERY_STATUS_FULL
    }
    .stateIn(GlobalScope, Eagerly, false)
