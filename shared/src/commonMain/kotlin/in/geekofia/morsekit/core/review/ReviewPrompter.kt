package `in`.geekofia.morsekit.core.review

import `in`.geekofia.morsekit.platform.KeyValueStore
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

/**
 * Decides when to ask for a store rating, so it happens at a good moment and rarely:
 * only after [minDaysSinceFirstUse] of use and [minSuccessfulUses] successful uses (copy, share,
 * transmit), and at most once per [cooldown]. Persists its counters in [store].
 * [nowMillis] (wall clock, epoch ms) is injectable for tests.
 */
class ReviewPrompter(
    private val store: KeyValueStore,
    private val nowMillis: () -> Long,
    private val minDaysSinceFirstUse: Duration = 3.days,
    private val minSuccessfulUses: Int = 5,
    private val cooldown: Duration = 120.days,
) {
    /** Call once per app start: remembers when MorseKit was first used. */
    fun recordAppStart() {
        if (store.getString(KEY_FIRST_USE_AT) == null) store.putString(KEY_FIRST_USE_AT, nowMillis().toString())
    }

    fun recordSuccessfulUse() {
        store.putInt(KEY_SUCCESSFUL_USES, successfulUses + 1)
    }

    /**
     * At a natural pause (after a copy, when a transmission ends): true if now is a good time to
     * ask. Returning true also records the attempt, so the cooldown starts even if the store
     * decides not to show its prompt.
     */
    fun takePromptOpportunity(): Boolean {
        val now = nowMillis()
        val firstUse = store.getString(KEY_FIRST_USE_AT)?.toLongOrNull() ?: return false
        val lastPrompt = store.getString(KEY_LAST_PROMPT_AT)?.toLongOrNull()
        val ready = now - firstUse >= minDaysSinceFirstUse.inWholeMilliseconds &&
            successfulUses >= minSuccessfulUses &&
            (lastPrompt == null || now - lastPrompt >= cooldown.inWholeMilliseconds)
        if (ready) store.putString(KEY_LAST_PROMPT_AT, now.toString())
        return ready
    }

    private val successfulUses: Int get() = store.getInt(KEY_SUCCESSFUL_USES) ?: 0

    internal companion object {
        const val KEY_FIRST_USE_AT = "review.firstUseAt"
        const val KEY_SUCCESSFUL_USES = "review.successfulUses"
        const val KEY_LAST_PROMPT_AT = "review.lastPromptAt"
    }
}
