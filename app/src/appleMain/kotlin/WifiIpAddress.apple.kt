package com.traviswyatt.qd

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import platform.darwin.freeifaddrs
import platform.darwin.getifaddrs
import platform.darwin.ifaddrs
import platform.posix.AF_INET
import platform.posix.sockaddr_in

@OptIn(ExperimentalForeignApi::class)
actual fun getWifiIpAddress(): String? {
    memScoped {
        val ifaddr = allocPointerTo<ifaddrs>()
        if (getifaddrs(ifaddr.ptr) == -1) {
            return null // Failed to retrieve the interface addresses
        }

        var address: String? = null
        var ptr = ifaddr.value

        while (ptr != null) {
            val ifa_name = ptr.pointed.ifa_name?.toKString() ?: continue
            val ifa_addr = ptr.pointed.ifa_addr ?: continue
            val family = ifa_addr.pointed.sa_family.toInt()

            if (family == AF_INET) {
                if (ifa_name == "en0") { // en0 is the interface for WiFi
                    val sockaddr = ifa_addr.reinterpret<sockaddr_in>()
                    val addrStruct = sockaddr.pointed.sin_addr
                    val addrBytes = addrStruct.s_addr.toByteArray()
                    address = addrBytes.joinToString(".") { it.toUByte().toString() }
                    break
                }
            }
            ptr = ptr.pointed.ifa_next
        }

        freeifaddrs(ifaddr.value)
        return address
    }
}

private fun UInt.toByteArray() = byteArrayOf(
    (this and 0xFFu).toByte(),
    ((this shr 8) and 0xFFu).toByte(),
    ((this shr 16) and 0xFFu).toByte(),
    ((this shr 24) and 0xFFu).toByte()
)
