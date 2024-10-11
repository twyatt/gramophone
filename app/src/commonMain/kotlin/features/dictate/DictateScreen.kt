package com.traviswyatt.qd.features.dictate

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.traviswyatt.qd.AppTheme
import com.traviswyatt.qd.HostFinder
import com.traviswyatt.qd.features.components.ActionRequired
import com.traviswyatt.qd.onLifecycleResumed
import com.traviswyatt.qd.transcript
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionState.Denied
import dev.icerock.moko.permissions.PermissionState.DeniedAlways
import dev.icerock.moko.permissions.PermissionState.Granted
import dev.icerock.moko.permissions.PermissionState.NotDetermined
import dev.icerock.moko.permissions.PermissionState.NotGranted
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory

class DictateScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel()
        onLifecycleResumed(screenModel::onResumed)

        AppTheme {
            Box(Modifier.background(color = MaterialTheme.colors.background).fillMaxSize()) {
                val isSearchingForHost by HostFinder.isRunning.collectAsState()
                if (isSearchingForHost) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                } else {
                    val needsHost by screenModel.needsHost.collectAsState(false)
                    if (needsHost) {
                        NeedsHost(screenModel::findHost)
                    } else {
                        val bluetoothPermissionState by screenModel.bluetoothPermissionState.collectAsState()
                        if (bluetoothPermissionState.canRequestPermission == false) {
                            BluetoothPermissionsDenied(screenModel::openAppSettings)
                        } else {
                            val recordPermissionState by screenModel.recordPermissionState.collectAsState()
                            when (recordPermissionState.canRequestPermission) {
                                true, null -> Transcription(screenModel)
                                false -> MicrophonePermissionsDenied(screenModel::openAppSettings)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun rememberScreenModel(): DictateScreenModel {
        val permissionsFactory = rememberPermissionsControllerFactory()
        val screenModel = rememberScreenModel {
            val permissionsController = permissionsFactory.createPermissionsController()
            DictateScreenModel(permissionsController)
        }
        BindEffect(screenModel.permissionsController)
        return screenModel
    }

}

@Composable
private fun BoxScope.NeedsHost(onClick: () -> Unit) {
    Column(
        Modifier.align(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Not associated with host.")
        Button(
            onClick = onClick,
            content = { Text("Search") },
        )
    }
}

@Composable
private fun MicrophonePermissionsDenied(onShowAppSettingsClick: () -> Unit) {
    ActionRequired(
        icon = Icons.Filled.Warning,
        contentDescription = "Microphone permissions required",
        description = "Microphone permissions are required for dictation. Please grant the permission.",
        buttonText = "Open Settings",
        onClick = onShowAppSettingsClick,
    )
}

@Composable
private fun BluetoothPermissionsDenied(onShowAppSettingsClick: () -> Unit) {
    ActionRequired(
        icon = Icons.Filled.Warning,
        contentDescription = "Bluetooth permissions required",
        description = "Bluetooth permissions are required for communicating with dictation host. Please grant the permission.",
        buttonText = "Open Settings",
        onClick = onShowAppSettingsClick,
    )
}

@Composable
private fun Transcription(screenModel: DictateScreenModel) {
    val isAvailable by screenModel.dictation.isAvailable.collectAsState()
    val isDictating by screenModel.dictation.isDictating.collectAsState(false)
    val transcript by transcript.collectAsState("")

    Box {
        val border = if (isDictating) Color.Green else Color.Transparent

        val fontSize = screenModel.fontSize.collectAsState()
        val lineHeight = derivedStateOf { (fontSize.value * 1.1f).toInt() }
        val transformState = rememberTransformableState { zoomChange, panChange, rotationChange ->
            screenModel.onZoomChange(zoomChange)
        }

        Box(
            Modifier
                .border(15.dp, border)
                .padding(15.dp)
                .fillMaxSize()
                .transformable(transformState)
                .clickable {
                    screenModel.toggleDictation()
                }
        ) {
            val message = if (isAvailable) {
                transcript
            } else {
                "⚠️ Dictation unavailable."
            }
            Text(message, fontSize = fontSize.value.toInt().sp, lineHeight = lineHeight.value.sp, fontWeight = FontWeight.Bold)
        }

//        val isSearching by hostFinder.isSearching.collectAsState()
//        if (isSearching) {
//            CircularProgressIndicator(Modifier.align(TopEnd).padding(5.dp))
//        }
    }
}

private val PermissionState.canRequestPermission: Boolean?
    get() = when (this) {
        NotDetermined, NotGranted, Denied -> true
        DeniedAlways -> false
        Granted -> null
    }
