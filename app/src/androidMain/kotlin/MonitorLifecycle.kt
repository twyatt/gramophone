package com.traviswyatt.qd

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

fun monitorLifecycle() {
    ProcessLifecycleOwner.get()
        .lifecycle
        .addObserver(object : LoggingDefaultLifecycleObserver() {
            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                isAppForegrounded.value = true
            }

            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                isAppForegrounded.value = false
            }
        })
}
