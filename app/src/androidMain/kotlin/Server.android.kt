package com.traviswyatt.gramophone

import io.ktor.server.netty.Netty

actual fun Server(): Server = KtorServer(Netty)
