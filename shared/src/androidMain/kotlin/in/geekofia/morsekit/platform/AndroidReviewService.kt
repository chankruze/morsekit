package `in`.geekofia.morsekit.platform

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager

/**
 * Google Play In-App Review. The sheet only appears for apps installed from Play, and Play applies
 * a quota, so a request may show nothing. Debuggable builds use [FakeReviewManager], which runs the
 * same flow without the real sheet, so it can be exercised before the app is published.
 */
internal class AndroidReviewService(
    private val context: Context,
    private val currentActivity: () -> Activity?,
) : ReviewService {
    private val manager: ReviewManager =
        if (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            FakeReviewManager(context)
        } else {
            ReviewManagerFactory.create(context)
        }

    override val storeName: String = "Google Play"

    override val canOpenStorePage: Boolean = true

    override fun requestReview() {
        manager.requestReviewFlow().addOnCompleteListener { request ->
            val activity = currentActivity() ?: return@addOnCompleteListener
            if (request.isSuccessful) manager.launchReviewFlow(activity, request.result)
        }
    }

    override fun openStorePage() {
        // The Play Store app if installed, otherwise the web listing.
        val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${StoreListing.PLAY_PACKAGE}"))
        try {
            context.startActivity(market.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: ActivityNotFoundException) {
            val web = Intent(Intent.ACTION_VIEW, Uri.parse(StoreListing.PLAY_URL))
            context.startActivity(web.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}
