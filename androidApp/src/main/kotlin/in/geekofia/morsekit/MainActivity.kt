package `in`.geekofia.morsekit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import `in`.geekofia.morsekit.platform.AppInfo
import `in`.geekofia.morsekit.platform.InMemoryKeyValueStore
import `in`.geekofia.morsekit.platform.PlatformServices
import `in`.geekofia.morsekit.platform.androidPlatformServices

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val platformServices = androidPlatformServices(this)
        setContent {
            App(platformServices)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(
        PlatformServices(
            clipboard = {},
            share = {},
            keyValueStore = InMemoryKeyValueStore(),
            appInfo = AppInfo(versionName = "preview", buildNumber = "0"),
        ),
    )
}
