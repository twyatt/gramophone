package com.traviswyatt.qd

import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import java.math.BigInteger
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteOrder

actual fun getWifiIpAddress(): String? {
    val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
    val ipAddress = wifiManager.connectionInfo.ipAddress.let {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            Integer.reverseBytes(it)
        } else {
            it
        }
    }.toLong()
        .let(BigInteger::valueOf)
        .toByteArray()

    return try {
        InetAddress.getByAddress(ipAddress).hostAddress
    } catch (ex: UnknownHostException) {
        null
    }
}
