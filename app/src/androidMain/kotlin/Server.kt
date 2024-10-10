package com.traviswyatt.qd

import io.ktor.server.netty.Netty

actual fun Server(): Server = KtorServer(Netty)
