package `in`.geekofia.morsekit

import androidx.compose.ui.window.ComposeUIViewController
import `in`.geekofia.morsekit.platform.IosPlatformServices
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    lateinit var controller: UIViewController
    val platformServices = IosPlatformServices(presenter = { controller })
    controller = ComposeUIViewController { App(platformServices) }
    return controller
}
