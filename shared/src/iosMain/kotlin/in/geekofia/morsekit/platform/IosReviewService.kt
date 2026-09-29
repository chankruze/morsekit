package `in`.geekofia.morsekit.platform

import platform.Foundation.NSURL
import platform.StoreKit.SKStoreReviewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

/**
 * StoreKit's review prompt (iOS shows it at most 3 times a year and may show nothing). The store
 * page opens only once [StoreListing.APP_STORE_ID] exists.
 */
internal class IosReviewService(private val presenter: () -> UIViewController?) : ReviewService {
    override val storeName: String = "the App Store"

    override val canOpenStorePage: Boolean get() = StoreListing.APP_STORE_ID != null

    override fun requestReview() {
        val scene = presenter()?.view?.window?.windowScene ?: return
        SKStoreReviewController.requestReviewInScene(scene)
    }

    override fun openStorePage() {
        val id = StoreListing.APP_STORE_ID ?: return
        val url = NSURL.URLWithString("https://apps.apple.com/app/id$id?action=write-review") ?: return
        UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any?>(), completionHandler = null)
    }
}
