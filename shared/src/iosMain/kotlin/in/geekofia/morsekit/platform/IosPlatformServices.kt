package `in`.geekofia.morsekit.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIPasteboard
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController

/** [presenter] supplies the view controller that hosts the Compose UI. */
fun iosPlatformServices(presenter: () -> UIViewController?): PlatformServices =
    PlatformServices(
        clipboard = IosClipboardService(),
        share = IosShareService(presenter),
        keyValueStore = IosKeyValueStore(),
        appInfo = iosAppInfo(),
    )

private class IosKeyValueStore : KeyValueStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getString(key: String): String? = defaults.stringForKey(key)

    override fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    override fun getInt(key: String): Int? =
        if (defaults.objectForKey(key) != null) defaults.integerForKey(key).toInt() else null

    override fun putInt(key: String, value: Int) {
        defaults.setInteger(value.toLong(), forKey = key)
    }
}

private fun iosAppInfo(): AppInfo {
    val bundle = NSBundle.mainBundle
    return AppInfo(
        versionName = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "unknown",
        buildNumber = bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String ?: "unknown",
    )
}

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
