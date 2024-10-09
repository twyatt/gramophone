package com.traviswyatt.qd

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object NopServer : Server {

    override val incoming: StateFlow<String?> = MutableStateFlow(null)

    override fun clear() {
        // No-op
    }

    override fun start() {
        // No-op
    }
}
