package `in`.geekofia.morsekit.platform

/**
 * The stores' own rating UI. Both stores require their native prompt (no custom "rate us"
 * modals), and both decide whether it actually appears (quotas, at most 3 times a year on iOS).
 */
interface ReviewService {
    /** Shown on the rating button, e.g. "Google Play". */
    val storeName: String

    /** False when there's no store page to open yet (iOS before an App Store ID exists). */
    val canOpenStorePage: Boolean

    /** Asks for the native in-app review prompt. The store may show nothing; that's expected. */
    fun requestReview()

    /** Opens the app's store page to write a review. Only for an explicit user tap. */
    fun openStorePage()
}

/** No store. For previews. */
class NoReviewService : ReviewService {
    override val storeName: String = ""
    override val canOpenStorePage: Boolean = false
    override fun requestReview() = Unit
    override fun openStorePage() = Unit
}
