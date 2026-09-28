package `in`.geekofia.morsekit

import android.app.Application
import `in`.geekofia.morsekit.platform.androidPlatformServices

/** Owns the app-scoped [AppContainer] so it survives Activity recreation (e.g. rotation). */
class MorseKitApplication : Application() {
    val container: AppContainer by lazy { AppContainer(androidPlatformServices(this)) }
}
