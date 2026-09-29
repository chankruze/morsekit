package `in`.geekofia.morsekit.core.update

/** How an update is installed. Flexible downloads in the background; immediate blocks the app. */
enum class UpdateMode { Flexible, Immediate }

/** What the store reports about a newer version. */
data class AvailableUpdate(
    val versionCode: Int,
    /** 0..5, set by the developer when publishing (Play Developer API). */
    val priority: Int,
    /** Days since the store first offered this update to the device, if known. */
    val stalenessDays: Int?,
    val flexibleAllowed: Boolean,
    val immediateAllowed: Boolean,
    /** An immediate update was started earlier and interrupted; it must be resumed. */
    val immediateInProgress: Boolean = false,
) {
    /** Important enough to block the app: a high-priority release, or one ignored for a long time. */
    val isUrgent: Boolean
        get() = priority >= URGENT_PRIORITY || (stalenessDays ?: 0) >= URGENT_STALENESS_DAYS

    companion object {
        const val URGENT_PRIORITY = 4
        const val URGENT_STALENESS_DAYS = 30
    }
}

/**
 * Flexible for normal releases; immediate only when [AvailableUpdate.isUrgent] (and allowed).
 * `null` when the store allows neither suitable type.
 */
fun chooseUpdateMode(update: AvailableUpdate): UpdateMode? = when {
    update.isUrgent && update.immediateAllowed -> UpdateMode.Immediate
    update.flexibleAllowed -> UpdateMode.Flexible
    else -> null
}
