package `in`.geekofia.morsekit.core.review

import `in`.geekofia.morsekit.platform.InMemoryKeyValueStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class ReviewPrompterTest {
    private val store = InMemoryKeyValueStore()
    private var now = 1_000_000_000_000L // an arbitrary wall-clock moment

    private fun prompter() = ReviewPrompter(store, nowMillis = { now })

    private fun advance(by: Duration) {
        now += by.inWholeMilliseconds
    }

    /** First use, then enough days and uses: the typical "due" situation. */
    private fun dueNow(): ReviewPrompter = prompter().apply {
        recordAppStart()
        advance(3.days)
        repeat(5) { recordSuccessfulUse() }
    }

    @Test
    fun asksOnceEnoughDaysAndUsesHavePassed() {
        assertTrue(dueNow().takePromptOpportunity())
    }

    @Test
    fun neverAsksWithoutAKnownFirstUse() {
        val prompter = prompter()
        repeat(10) { prompter.recordSuccessfulUse() }
        assertFalse(prompter.takePromptOpportunity())
    }

    @Test
    fun waitsForEnoughDays() {
        val prompter = prompter().apply { recordAppStart() }
        repeat(20) { prompter.recordSuccessfulUse() }
        advance(3.days - 1.hours)
        assertFalse(prompter.takePromptOpportunity())
        advance(1.hours)
        assertTrue(prompter.takePromptOpportunity())
    }

    @Test
    fun waitsForEnoughUses() {
        val prompter = prompter().apply { recordAppStart() }
        advance(30.days)
        repeat(4) { prompter.recordSuccessfulUse() }
        assertFalse(prompter.takePromptOpportunity())
        prompter.recordSuccessfulUse()
        assertTrue(prompter.takePromptOpportunity())
    }

    @Test
    fun respectsTheCooldownBetweenPrompts() {
        val prompter = dueNow()
        assertTrue(prompter.takePromptOpportunity())
        assertFalse(prompter.takePromptOpportunity())
        advance(119.days)
        assertFalse(prompter.takePromptOpportunity())
        advance(1.days)
        assertTrue(prompter.takePromptOpportunity())
    }

    @Test
    fun firstUseIsRecordedOnlyOnce() {
        prompter().recordAppStart()
        advance(10.days)
        prompter().recordAppStart() // a later launch must not reset the clock
        repeat(5) { prompter().recordSuccessfulUse() }
        assertTrue(prompter().takePromptOpportunity())
    }

    @Test
    fun countersSurviveAppRestarts() {
        dueNow().takePromptOpportunity()
        // A new instance on the same store remembers the last prompt.
        assertFalse(prompter().takePromptOpportunity())
        assertEquals(5, store.getInt(ReviewPrompter.KEY_SUCCESSFUL_USES))
    }
}
