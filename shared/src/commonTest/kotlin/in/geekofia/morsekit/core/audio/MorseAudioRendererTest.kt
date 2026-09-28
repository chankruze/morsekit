package `in`.geekofia.morsekit.core.audio

import `in`.geekofia.morsekit.core.morse.MorseCodec
import `in`.geekofia.morsekit.core.timing.MorseTiming
import `in`.geekofia.morsekit.core.timing.toSignals
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class MorseAudioRendererTest {
    private val codec = MorseCodec()
    private val rate = MorseAudioRenderer.SAMPLE_RATE
    private val padding = (MorseAudioRenderer.PADDING.inWholeMilliseconds * rate / 1000).toInt()

    private fun message(text: String) = codec.encode(text).message

    private fun render(text: String, wpm: Int = 20, hz: Int = 600) =
        MorseAudioRenderer.render(message(text), MorseTiming(wpm), hz)

    private fun tonesOf(text: String, wpm: Int = 20) =
        scheduleTones(message(text).toSignals(), MorseTiming(wpm), rate).tones

    @Test
    fun lengthIsScheduleplusPadding() {
        val audio = render("SOS")
        val schedule = scheduleTones(message("SOS").toSignals(), MorseTiming(20), rate)
        assertEquals(padding + schedule.totalFrames + padding, audio.frameCount)
        assertEquals(rate, audio.sampleRate)
    }

    @Test
    fun durationMatchesMorseTiming() {
        val timing = MorseTiming(20)
        val expected = timing.totalDuration(message("PARIS").toSignals()) + MorseAudioRenderer.PADDING * 2
        val actual = render("PARIS").duration
        assertTrue((actual - expected).absoluteValue < 1.milliseconds, "expected $expected, was $actual")
    }

    @Test
    fun gapsAndPaddingAreExactSilence() {
        val audio = render("ET")
        val tones = tonesOf("ET").map { (it.startFrame + padding) until (it.endFrame + padding) }
        audio.samples.forEachIndexed { i, sample ->
            if (tones.none { i in it }) assertEquals(0f, sample, "sample $i should be silent")
        }
    }

    @Test
    fun tonesReachButNeverExceedTheAmplitude() {
        val audio = render("T")
        val peak = audio.samples.maxOf { abs(it) }
        assertTrue(peak <= MorseAudioRenderer.AMPLITUDE + 1e-6f)
        assertTrue(peak > MorseAudioRenderer.AMPLITUDE * 0.99f)
    }

    @Test
    fun tonesFadeInAndOutToAvoidClicks() {
        val audio = render("T")
        val tone = tonesOf("T").single()
        val first = audio.samples[padding + tone.startFrame + 1]
        val last = audio.samples[padding + tone.endFrame - 1]
        assertTrue(abs(first) < 0.01f, "tone should start near silence, was $first")
        assertTrue(abs(last) < 0.01f, "tone should end near silence, was $last")
    }

    @Test
    fun pitchMatchesTheRequestedFrequency() {
        listOf(400, 600, 1000).forEach { hz ->
            val audio = render("T", wpm = 5, hz = hz) // 720 ms dash
            val tone = tonesOf("T", wpm = 5).single()
            val samples = audio.samples.copyOfRange(padding + tone.startFrame, padding + tone.endFrame)
            val crossings = samples.toList().zipWithNext().count { (a, b) -> a < 0f && b >= 0f }
            val seconds = samples.size.toDouble() / rate
            val measured = crossings / seconds
            assertTrue(abs(measured - hz) < hz * 0.02, "expected ~$hz Hz, measured $measured Hz")
        }
    }

    @Test
    fun fasterSpeedGivesShorterAudio() {
        assertTrue(render("SOS", wpm = 30).frameCount < render("SOS", wpm = 10).frameCount)
    }

    @Test
    fun emptyMessageIsOnlyPadding() {
        val audio = MorseAudioRenderer.render(message(""), MorseTiming(), 600)
        assertEquals(2 * padding, audio.frameCount)
        assertTrue(audio.samples.all { it == 0f })
    }

    @Test
    fun longMessagesAreRejected() {
        val long = message("PARIS ".repeat(30)) // 30 words at 5 WPM = 6 minutes
        assertTrue(MorseAudioRenderer.isTooLongToPlay(long, MorseTiming(5)))
        assertFalse(MorseAudioRenderer.isTooLongToPlay(long, MorseTiming(20)))
        assertFailsWith<IllegalArgumentException> { MorseAudioRenderer.render(long, MorseTiming(5), 600) }
    }

    @Test
    fun frequencyMustBeBelowNyquist() {
        assertFailsWith<IllegalArgumentException> { MorseAudioRenderer.render(message("E"), MorseTiming(), rate / 2) }
    }
}
