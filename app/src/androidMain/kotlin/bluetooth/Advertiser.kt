package com.traviswyatt.gramophone.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.AdvertiseSettings.ADVERTISE_MODE_LOW_POWER
import android.bluetooth.le.AdvertiseSettings.ADVERTISE_TX_POWER_LOW
import android.bluetooth.le.BluetoothLeAdvertiser
import android.os.ParcelUuid
import androidx.annotation.RequiresPermission
import com.juul.khronicle.Log
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

private const val TAG = "Advertiser"

object Advertiser {

    private val advertiser: BluetoothLeAdvertiser = BluetoothAdapter
        .getDefaultAdapter()
        .bluetoothLeAdvertiser
        ?: error("BluetoothLeAdvertiser unavailable")

    private val settings = AdvertiseSettings.Builder()
        .setConnectable(false)
        .setAdvertiseMode(ADVERTISE_MODE_LOW_POWER)
        .setTxPowerLevel(ADVERTISE_TX_POWER_LOW)
        .build()

    @OptIn(ExperimentalStdlibApi::class) // For `toHexString`.
    @RequiresPermission(value = "android.permission.BLUETOOTH_ADVERTISE")
    suspend fun advertise(data: ByteArray) {
        suspendCancellableCoroutine<Nothing> { continuation ->
            val callback = object : AdvertiseCallback() {
                override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                    Log.info(tag = TAG) { "Advertising started: ${data.toHexString()}" }
                }
                override fun onStartFailure(errorCode: Int) {
                    continuation.resumeWithException(
                        IllegalStateException("startAdvertising failed with error code $errorCode")
                    )
                }
            }

            continuation.invokeOnCancellation {
                Log.info(tag = TAG) { "Stop advertising: ${data.toHexString()}" }
                advertiser.stopAdvertising(callback)
            }
            Log.info(tag = TAG) { "Starting advertisement: ${data.toHexString()}" }
            advertiser.startAdvertising(settings, advertiseDataOf(data), callback)
        }
    }
}

private fun advertiseDataOf(data: ByteArray): AdvertiseData {
    return AdvertiseData.Builder()
        .setIncludeDeviceName(false)
        .addServiceData(ParcelUuid.fromString(ServiceUuid), data)
        .build()
}
