package `in`.geekofia.morsekit.core.timing

import `in`.geekofia.morsekit.core.model.MorseMessage
import `in`.geekofia.morsekit.core.morse.MorseCodec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class MorseTimingTest {
    private val codec = MorseCodec()

    private fun signals(text: String) = codec.encode(text).message.toSignals()

    private fun on(units: Int) = MorseSignal(isOn = true, units = units)
    private fun off(units: Int) = MorseSignal(isOn = false, units = units)

    @Test
    fun unitFollowsParisStandard() {
        assertEquals(60.milliseconds, MorseTiming(20).unit)
        assertEquals(240.milliseconds, MorseTiming(5).unit)
        assertEquals(20.milliseconds, MorseTiming(60).unit)
    }

    @Test
    fun rejectsOutOfRangeSpeeds() {
        assertFailsWith<IllegalArgumentException> { MorseTiming(0) }
        assertFailsWith<IllegalArgumentException> { MorseTiming(MorseTiming.MAX_WPM + 1) }
    }

    @Test
    fun singleLetterUsesElementGaps() {
        assertEquals(listOf(on(1), off(1), on(3)), signals("A"))
    }

    @Test
    fun lettersAreSeparatedByThreeUnits() {
        assertEquals(listOf(on(1), off(3), on(3)), signals("ET"))
    }

    @Test
    fun wordsAreSeparatedBySevenUnits() {
        assertEquals(listOf(on(1), off(7), on(1)), signals("E E"))
    }

    @Test
    fun emptyMessageHasNoSignals() {
        assertEquals(emptyList(), MorseMessage.Empty.toSignals())
    }

    @Test
    fun parisIsFiftyUnitsIncludingTrailingWordGap() {
        // By definition "PARIS " is 50 units; the trailing word gap isn't emitted.
        val units = signals("PARIS").sumOf { it.units }
        assertEquals(50 - MorseSignal.WORD_GAP_UNITS, units)
    }

    @Test
    fun durationsScaleWithUnit() {
        val timing = MorseTiming(20)
        assertEquals(180.milliseconds, timing.durationOf(on(3)))
        assertEquals(timing.unit * 5, timing.totalDuration(signals("A")))
        assertEquals(Duration.ZERO, timing.totalDuration(emptyList()))
    }
}
