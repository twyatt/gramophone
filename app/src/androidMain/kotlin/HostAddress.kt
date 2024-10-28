package com.traviswyatt.gramophone

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import androidx.core.content.ContextCompat
import java.math.BigInteger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

val hostAddress = callbackFlow {
    val networkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()

    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities,
        ) {
            val transportInfo = networkCapabilities.transportInfo
            if (transportInfo != null) {
                transportInfo as WifiInfo
                val bytes = transportInfo.ipAddress
                    .toLong()
                    .let(BigInteger::valueOf)
                    .toByteArray()
                    .reversed()
                    .toByteArray()
                trySendBlocking(bytes)
            } else {
                trySendBlocking(null)
            }
        }

        override fun onLost(network: Network) {
            trySendBlocking(null)
        }
    }

    val connectivityManager = ContextCompat.getSystemService(
        applicationContext,
        ConnectivityManager::class.java
    ) ?: error("Unable to obtain ConnectivityManager")
    connectivityManager.requestNetwork(networkRequest, callback)

    awaitClose {
        connectivityManager.unregisterNetworkCallback(callback)
    }
}.distinctUntilChanged { old, new -> old.contentEquals(new) }
