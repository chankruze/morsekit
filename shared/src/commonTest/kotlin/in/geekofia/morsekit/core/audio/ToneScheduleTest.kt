package `in`.geekofia.morsekit.core.audio

import `in`.geekofia.morsekit.core.morse.MorseCodec
import `in`.geekofia.morsekit.core.timing.MorseSignal
import `in`.geekofia.morsekit.core.timing.MorseTiming
import `in`.geekofia.morsekit.core.timing.toSignals
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ToneScheduleTest {
    private val codec = MorseCodec()
    private val rate = 16_000

    private fun schedule(text: String, wpm: Int) =
        scheduleTones(codec.encode(text).message.toSignals(), MorseTiming(wpm), rate)

    @Test
    fun dotLastsOneUnit() {
        // 20 WPM → 60 ms per unit → 960 frames at 16 kHz.
        assertEquals(ToneSchedule(listOf(ToneSegment(0, 960)), 960), schedule("E", 20))
    }

    @Test
    fun letterUsesElementGapAndDash() {
        // A = dot (1), gap (1), dash (3)
        assertEquals(ToneSchedule(listOf(ToneSegment(0, 960), ToneSegment(1920, 4800)), 4800), schedule("A", 20))
    }

    @Test
    fun letterAndWordGapsAreThreeAndSevenUnits() {
        val et = schedule("ET", 20).tones
        assertEquals(3 * 960, et[1].startFrame - et[0].endFrame)
        val eSpaceE = schedule("E E", 20).tones
        assertEquals(7 * 960, eSpaceE[1].startFrame - eSpaceE[0].endFrame)
    }

    @Test
    fun speedScalesEveryDuration() {
        val slow = schedule("PARIS", 10)
        val fast = schedule("PARIS", 20)
        assertEquals(slow.totalFrames, fast.totalFrames * 2)
        slow.tones.zip(fast.tones).forEach { (s, f) -> assertEquals(s.frameCount, f.frameCount * 2) }
    }

    @Test
    fun roundingNeverAccumulates() {
        // 13 WPM → 92.307... ms per unit → 1476.92... frames: not a whole number.
        val timing = MorseTiming(13)
        val signals = codec.encode("THE QUICK BROWN FOX JUMPS OVER THE LAZY DOG 0123456789").message.toSignals()
        val result = scheduleTones(signals, timing, rate)
        val framesPerUnit = 1200.0 / 13 / 1000 * rate

        var units = 0
        var toneIndex = 0
        for (signal in signals) {
            val exactStart = units * framesPerUnit
            units += signal.units
            if (signal.isOn) {
                val tone = result.tones[toneIndex++]
                assertTrue(abs(tone.startFrame - exactStart) <= 0.5, "start drifted at unit $units")
                assertTrue(abs(tone.endFrame - units * framesPerUnit) <= 0.5, "end drifted at unit $units")
            }
        }
        assertEquals((units * framesPerUnit).roundToInt(), result.totalFrames)
    }

    @Test
    fun emptySignalsGiveEmptySchedule() {
        assertEquals(ToneSchedule(emptyList(), 0), scheduleTones(emptyList(), MorseTiming(), rate))
    }

    @Test
    fun onlyOnSignalsBecomeTones() {
        val signals = listOf(MorseSignal(true, 3), MorseSignal(false, 7), MorseSignal(true, 1))
        assertEquals(2, scheduleTones(signals, MorseTiming(20), rate).tones.size)
    }
}
