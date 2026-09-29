package `in`.geekofia.morsekit

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import `in`.geekofia.morsekit.platform.AppInfo
import `in`.geekofia.morsekit.platform.InMemoryKeyValueStore
import `in`.geekofia.morsekit.platform.NoAppUpdateService
import `in`.geekofia.morsekit.platform.NoReviewService
import `in`.geekofia.morsekit.platform.NoTorchController
import `in`.geekofia.morsekit.platform.NoVibrationController
import `in`.geekofia.morsekit.platform.NoOpPcmAudioPlayer
import `in`.geekofia.morsekit.platform.PlatformServices

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val container = (application as MorseKitApplication).container
        setContent {
            App(container, onSystemBarAppearanceChange = ::applySystemBarStyle, onExit = ::exitApp)
        }
    }

    /**
     * What the system does on back from the launcher activity: since Android 12 the task moves to
     * the background (the app stays warm for a fast relaunch); before that the activity finishes.
     */
    private fun exitApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) moveTaskToBack(true) else finish()
    }

    /**
     * Keeps status/navigation bar icons readable over the app's own colours (which follow the
     * in-app theme, not necessarily the system's): both bars are transparent over app content.
     */
    private fun applySystemBarStyle(appearance: SystemBarAppearance) {
        enableEdgeToEdge(
            statusBarStyle = if (appearance.lightStatusBarIcons) {
                SystemBarStyle.dark(Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            },
            navigationBarStyle = if (appearance.lightNavigationBarIcons) {
                SystemBarStyle.dark(DARK_SCRIM)
            } else {
                SystemBarStyle.light(LIGHT_SCRIM, DARK_SCRIM)
            },
        )
    }

    private companion object {
        // The defaults enableEdgeToEdge() uses for the 3-button navigation bar.
        val LIGHT_SCRIM = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
        val DARK_SCRIM = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    val services = PlatformServices(
        clipboard = {},
        share = {},
        keyValueStore = InMemoryKeyValueStore(),
        appInfo = AppInfo(versionName = "preview", buildNumber = "0"),
        audioPlayer = NoOpPcmAudioPlayer(),
        torch = NoTorchController(),
        vibration = NoVibrationController(),
        review = NoReviewService(),
        appUpdates = NoAppUpdateService(),
    )
    App(AppContainer(services))
}
