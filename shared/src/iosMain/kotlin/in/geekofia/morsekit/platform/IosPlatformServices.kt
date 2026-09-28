package `in`.geekofia.morsekit.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIPasteboard
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController

/** [presenter] supplies the view controller that hosts the Compose UI. */
fun IosPlatformServices(presenter: () -> UIViewController?): PlatformServices =
    PlatformServices(
        clipboard = IosClipboardService(),
        share = IosShareService(presenter),
    )

private class IosClipboardService : ClipboardService {
    override fun copyText(text: String) {
        UIPasteboard.generalPasteboard.string = text
    }
}

private class IosShareService(private val presenter: () -> UIViewController?) : ShareService {
    @OptIn(ExperimentalForeignApi::class)
    override fun shareText(text: String) {
        val host = presenter()?.topmostPresented() ?: return
        val activity = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
        // iPad presents the share sheet as a popover, which crashes without an anchor.
        activity.popoverPresentationController?.let { popover ->
            popover.sourceView = host.view
            popover.sourceRect = host.view.bounds.useContents {
                CGRectMake(size.width / 2, size.height / 2, 0.0, 0.0)
            }
        }
        host.presentViewController(activity, animated = true, completion = null)
    }

    private fun UIViewController.topmostPresented(): UIViewController {
        var current = this
        while (true) {
            current = current.presentedViewController ?: return current
        }
    }
}
