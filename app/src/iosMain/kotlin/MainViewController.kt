package com.traviswyatt.gramophone

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    configureLogging()
    init()
    return ComposeUIViewController {
        App()
    }
}
