package `in`.geekofia.morsekit

import androidx.compose.ui.window.ComposeUIViewController
import `in`.geekofia.morsekit.platform.iosPlatformServices
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    lateinit var controller: UIViewController
    // Called once per app launch, so this is the app-scoped container on iOS.
    val container = AppContainer(iosPlatformServices(presenter = { controller }))
    controller = ComposeUIViewController { App(container) }
    return controller
}
