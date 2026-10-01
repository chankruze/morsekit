package `in`.geekofia.morsekit.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.UIKit.UIApplication

@Composable
actual fun KeepScreenOn() {
    DisposableEffect(Unit) {
        UIApplication.sharedApplication.idleTimerDisabled = true
        onDispose { UIApplication.sharedApplication.idleTimerDisabled = false }
    }
}

/** iOS doesn't recreate the UI on rotation, so leaving is always real. */
@Composable
actual fun rememberIsChangingConfigurations(): () -> Boolean = { false }
