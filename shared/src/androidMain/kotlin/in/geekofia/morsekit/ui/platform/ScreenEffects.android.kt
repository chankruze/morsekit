package `in`.geekofia.morsekit.ui.platform

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

@Composable
actual fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}

/** True while the Activity is being destroyed only to be recreated (e.g. on rotation). */
@Composable
actual fun rememberIsChangingConfigurations(): () -> Boolean {
    val activity = LocalContext.current.findActivity()
    return remember(activity) { { activity?.isChangingConfigurations == true } }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
