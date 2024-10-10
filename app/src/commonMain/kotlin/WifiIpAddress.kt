package com.traviswyatt.qd

expect fun getWifiIpAddress(): String?

val wifiIpAddress: String? by lazy(::getWifiIpAddress)
