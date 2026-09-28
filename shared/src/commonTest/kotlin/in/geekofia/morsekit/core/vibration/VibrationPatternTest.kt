package `in`.geekofia.morsekit.core.vibration

import `in`.geekofia.morsekit.core.morse.MorseCodec
import `in`.geekofia.morsekit.core.timing.MorseTiming
import `in`.geekofia.morsekit.core.timing.toSignals
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VibrationPatternTest {
    private val codec = MorseCodec()

    private fun pattern(text: String, wpm: Int = 20) = VibrationPattern.of(codec.encode(text).message, MorseTiming(wpm))

    private fun on(ms: Long) = VibrationSegment(true, ms)
    private fun off(ms: Long) = VibrationSegment(false, ms)

    @Test
    fun letterIsDotGapDash() {
        // 20 WPM → 60 ms unit. A = on 60, off 60, on 180.
        assertEquals(listOf(on(60), off(60), on(180)), pattern("A").segments)
    }

    @Test
    fun letterAndWordGaps() {
        assertEquals(listOf(on(60), off(180), on(180)), pattern("ET").segments)
        assertEquals(listOf(on(60), off(420), on(60)), pattern("E E").segments)
    }

    @Test
    fun startsOnAndAlternates() {
        val segments = pattern("HELLO WORLD").segments
        assertTrue(segments.first().isOn)
        assertTrue(segments.last().isOn)
        segments.zipWithNext().forEach { (a, b) -> assertTrue(a.isOn != b.isOn) }
    }

    @Test
    fun speedScalesEverySegment() {
        val slow = pattern("SOS", wpm = 10).segments
        val fast = pattern("SOS", wpm = 20).segments
        slow.zip(fast).forEach { (s, f) -> assertEquals(s.durationMillis, f.durationMillis * 2) }
    }

    @Test
    fun millisecondRoundingNeverAccumulates() {
        // 13 WPM → 92.307... ms per unit: not a whole number of milliseconds.
        val timing = MorseTiming(13)
        val signals = codec.encode("THE QUICK BROWN FOX JUMPS OVER THE LAZY DOG").message.toSignals()
        val segments = VibrationPattern.of(signals, timing).segments
        val unitMillis = 1200.0 / 13

        var units = 0
        var elapsedMillis = 0L
        signals.zip(segments).forEach { (signal, segment) ->
            units += signal.units
            elapsedMillis += segment.durationMillis
            assertTrue(abs(elapsedMillis - units * unitMillis) <= 0.5, "drifted at unit $units")
            assertEquals(signal.isOn, segment.isOn)
        }
    }

    @Test
    fun durationMatchesMorseTiming() {
        val message = codec.encode("PARIS").message
        val timing = MorseTiming(20)
        assertEquals(timing.totalDuration(message.toSignals()), VibrationPattern.of(message, timing).duration)
    }

    @Test
    fun emptyMessageGivesEmptyPattern() {
        assertTrue(pattern("").segments.isEmpty())
        assertEquals(0L, pattern("").durationMillis)
    }

    @Test
    fun segmentsMustLastAtLeastOneMillisecond() {
        assertFailsWith<IllegalArgumentException> { VibrationSegment(true, 0) }
    }

    @Test
    fun longMessagesAreRejected() {
        val long = codec.encode("PARIS ".repeat(30)).message // 6 minutes at 5 WPM
        assertTrue(VibrationPattern.isTooLong(long, MorseTiming(5)))
        assertFalse(VibrationPattern.isTooLong(long, MorseTiming(20)))
    }
}
