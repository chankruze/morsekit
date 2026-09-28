package `in`.geekofia.morsekit.core.audio

import `in`.geekofia.morsekit.core.model.MorseMessage
import `in`.geekofia.morsekit.core.timing.MorseTiming
import `in`.geekofia.morsekit.core.timing.toSignals
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

/** Renders Morse as a sine tone. Pure and platform-independent; platforms only play the result. */
object MorseAudioRenderer {
    /** Plenty for a pure tone of at most 1000 Hz, and keeps buffers small. */
    const val SAMPLE_RATE = 16_000

    /** Peak level, leaving headroom so the tone isn't harsh or clipped. */
    const val AMPLITUDE = 0.5f

    /** Fade in/out of each tone. Without it, the hard edges are audible as clicks. */
    val RAMP = 5.milliseconds

    /** Silence before and after, so audio start-up latency doesn't swallow the first dot. */
    val PADDING = 100.milliseconds

    /** Longest message that will be rendered; bounds memory (about 4 MB per minute). */
    val MAX_DURATION = 5.minutes

    fun isTooLongToPlay(message: MorseMessage, timing: MorseTiming): Boolean =
        timing.totalDuration(message.toSignals()) > MAX_DURATION

    fun render(
        message: MorseMessage,
        timing: MorseTiming,
        frequencyHz: Int,
        sampleRate: Int = SAMPLE_RATE,
    ): PcmAudio {
        require(!isTooLongToPlay(message, timing)) { "Message is longer than $MAX_DURATION" }
        require(frequencyHz in 1 until sampleRate / 2) { "Frequency must be below the Nyquist limit" }

        val schedule = scheduleTones(message.toSignals(), timing, sampleRate)
        val padding = framesFor(PADDING.inWholeMilliseconds, sampleRate)
        val rampFrames = framesFor(RAMP.inWholeMilliseconds, sampleRate)
        val samples = FloatArray(padding + schedule.totalFrames + padding)
        val phaseStep = 2 * PI * frequencyHz / sampleRate

        for (tone in schedule.tones) {
            val length = tone.frameCount
            val ramp = min(rampFrames, length / 2)
            for (i in 0 until length) {
                val envelope = when {
                    i < ramp -> raisedCosine(i.toDouble() / ramp)
                    i >= length - ramp -> raisedCosine((length - i).toDouble() / ramp)
                    else -> 1.0
                }
                samples[padding + tone.startFrame + i] = (AMPLITUDE * envelope * sin(phaseStep * i)).toFloat()
            }
        }
        return PcmAudio(samples, sampleRate)
    }

    /** 0 at 0, 1 at 1, smooth in between. */
    private fun raisedCosine(x: Double): Double = 0.5 * (1 - cos(PI * x))

    private fun framesFor(millis: Long, sampleRate: Int): Int = (millis * sampleRate / 1000).toInt()
}
