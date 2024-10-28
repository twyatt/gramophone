package com.traviswyatt.gramophone.bluetooth

import android.bluetooth.le.AdvertisingSet
import android.bluetooth.le.AdvertisingSetCallback

abstract class AdvertisingSetCallbackStub : AdvertisingSetCallback() {
    override fun onAdvertisingSetStarted(
        advertisingSet: AdvertisingSet,
        txPower: Int,
        status: Int,
    ) {}
    override fun onAdvertisingDataSet(advertisingSet: AdvertisingSet, status: Int) {}
    override fun onScanResponseDataSet(advertisingSet: AdvertisingSet, status: Int) {}
    override fun onAdvertisingSetStopped(advertisingSet: AdvertisingSet) {}
}
