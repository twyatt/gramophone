package com.traviswyatt.qd

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.juul.khronicle.Log

private const val TAG = "ProcessLifecycleOwner"

open class LoggingDefaultLifecycleObserver : DefaultLifecycleObserver {
    override fun onCreate(owner: LifecycleOwner) {
        Log.verbose(tag = TAG) { "onCreate" }
    }

    override fun onStart(owner: LifecycleOwner) {
        Log.verbose(tag = TAG) { "onStart" }
    }

    override fun onResume(owner: LifecycleOwner) {
        Log.verbose(tag = TAG) { "onResume" }
    }

    override fun onPause(owner: LifecycleOwner) {
        Log.verbose(tag = TAG) { "onPause" }
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.verbose(tag = TAG) { "onStop" }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Log.verbose(tag = TAG) { "onDestroy" }
    }
}
