package `in`.geekofia.morsekit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import `in`.geekofia.morsekit.platform.AndroidPlatformServices
import `in`.geekofia.morsekit.platform.PlatformServices

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val platformServices = AndroidPlatformServices(this)
        setContent {
            App(platformServices)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(PlatformServices(clipboard = {}, share = {}))
}
