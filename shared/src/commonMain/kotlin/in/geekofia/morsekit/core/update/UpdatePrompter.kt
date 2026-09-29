package `in`.geekofia.morsekit.core.update

import `in`.geekofia.morsekit.platform.KeyValueStore
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

/**
 * Keeps update prompts from nagging: new updates are offered at most once per [checkInterval],
 * and a version the user declined isn't offered again for [snooze]. Urgent updates and manual
 * checks ("Check for updates") ignore the snooze. [nowMillis] (wall clock) is injectable.
 */
class UpdatePrompter(
    private val store: KeyValueStore,
    private val nowMillis: () -> Long,
    private val checkInterval: Duration = 1.days,
    private val snooze: Duration = 7.days,
) {
    /** True (and records the check) if it's time to look for new updates automatically. */
    fun takeCheckOpportunity(): Boolean {
        val now = nowMillis()
        val last = store.getString(KEY_LAST_CHECK_AT)?.toLongOrNull()
        if (last != null && now - last < checkInterval.inWholeMilliseconds) return false
        store.putString(KEY_LAST_CHECK_AT, now.toString())
        return true
    }

    /** The mode to offer [update] in, or null if it shouldn't be offered right now. */
    fun modeToOffer(update: AvailableUpdate, manual: Boolean): UpdateMode? {
        val mode = chooseUpdateMode(update) ?: return null
        if (manual || update.isUrgent) return mode
        return if (isSnoozed(update.versionCode)) null else mode
    }

    /** The user said "not now" to [versionCode]. */
    fun onDeclined(versionCode: Int) {
        store.putInt(KEY_SNOOZED_VERSION, versionCode)
        store.putString(KEY_SNOOZED_AT, nowMillis().toString())
    }

    private fun isSnoozed(versionCode: Int): Boolean {
        if (store.getInt(KEY_SNOOZED_VERSION) != versionCode) return false
        val at = store.getString(KEY_SNOOZED_AT)?.toLongOrNull() ?: return false
        return nowMillis() - at < snooze.inWholeMilliseconds
    }

    internal companion object {
        const val KEY_LAST_CHECK_AT = "update.lastCheckAt"
        const val KEY_SNOOZED_VERSION = "update.snoozedVersion"
        const val KEY_SNOOZED_AT = "update.snoozedAt"
    }
}
