package `in`.geekofia.morsekit

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import `in`.geekofia.morsekit.platform.AppInfo
import `in`.geekofia.morsekit.platform.InMemoryKeyValueStore
import `in`.geekofia.morsekit.platform.PlatformServices

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val container = (application as MorseKitApplication).container
        setContent {
            App(container, onDarkThemeChange = ::applySystemBarStyle)
        }
    }

    /** Keeps status/navigation bar icons readable when the app theme differs from the system's. */
    private fun applySystemBarStyle(darkTheme: Boolean) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
            navigationBarStyle = SystemBarStyle.auto(LIGHT_SCRIM, DARK_SCRIM) { darkTheme },
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
    )
    App(AppContainer(services))
}
