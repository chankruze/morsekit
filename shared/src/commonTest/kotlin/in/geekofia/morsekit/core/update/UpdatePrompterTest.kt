package `in`.geekofia.morsekit.core.update

import `in`.geekofia.morsekit.platform.InMemoryKeyValueStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

internal fun update(
    versionCode: Int = 5,
    priority: Int = 0,
    stalenessDays: Int? = 0,
    flexibleAllowed: Boolean = true,
    immediateAllowed: Boolean = true,
    immediateInProgress: Boolean = false,
) = AvailableUpdate(versionCode, priority, stalenessDays, flexibleAllowed, immediateAllowed, immediateInProgress)

class ChooseUpdateModeTest {
    @Test fun normalReleaseIsFlexible() = assertEquals(UpdateMode.Flexible, chooseUpdateMode(update()))
    @Test fun highPriorityIsImmediate() = assertEquals(UpdateMode.Immediate, chooseUpdateMode(update(priority = 4)))
    @Test fun longIgnoredIsImmediate() = assertEquals(UpdateMode.Immediate, chooseUpdateMode(update(stalenessDays = 30)))
    @Test fun notYetUrgent() = assertEquals(UpdateMode.Flexible, chooseUpdateMode(update(priority = 3, stalenessDays = 29)))
    @Test fun urgentFallsBackToFlexibleIfImmediateNotAllowed() =
        assertEquals(UpdateMode.Flexible, chooseUpdateMode(update(priority = 5, immediateAllowed = false)))
    @Test fun nothingAllowedMeansNoOffer() =
        assertNull(chooseUpdateMode(update(flexibleAllowed = false, immediateAllowed = false)))
    @Test fun notUrgentAndOnlyImmediateAllowedIsNotForced() =
        assertNull(chooseUpdateMode(update(flexibleAllowed = false)))
}

class UpdatePrompterTest {
    private val store = InMemoryKeyValueStore()
    private var now = 1_000_000_000_000L
    private val prompter = UpdatePrompter(store, nowMillis = { now })

    private fun advance(by: Duration) {
        now += by.inWholeMilliseconds
    }

    @Test
    fun checksAtMostOnceADay() {
        assertTrue(prompter.takeCheckOpportunity())
        assertFalse(prompter.takeCheckOpportunity())
        advance(1.days - 1.hours)
        assertFalse(prompter.takeCheckOpportunity())
        advance(1.hours)
        assertTrue(prompter.takeCheckOpportunity())
    }

    @Test
    fun declinedVersionIsSnoozedForAWeek() {
        prompter.onDeclined(5)
        assertNull(prompter.modeToOffer(update(versionCode = 5), manual = false))
        advance(7.days - 1.hours)
        assertNull(prompter.modeToOffer(update(versionCode = 5), manual = false))
        advance(1.hours)
        assertEquals(UpdateMode.Flexible, prompter.modeToOffer(update(versionCode = 5), manual = false))
    }

    @Test
    fun aNewerVersionIsNotSnoozed() {
        prompter.onDeclined(5)
        assertEquals(UpdateMode.Flexible, prompter.modeToOffer(update(versionCode = 6), manual = false))
    }

    @Test
    fun urgentAndManualIgnoreTheSnooze() {
        prompter.onDeclined(5)
        assertEquals(UpdateMode.Immediate, prompter.modeToOffer(update(versionCode = 5, priority = 5), manual = false))
        assertEquals(UpdateMode.Flexible, prompter.modeToOffer(update(versionCode = 5), manual = true))
    }
}
