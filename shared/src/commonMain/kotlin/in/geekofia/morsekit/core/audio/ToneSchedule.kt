package `in`.geekofia.morsekit.core.audio

import `in`.geekofia.morsekit.core.timing.MorseSignal
import `in`.geekofia.morsekit.core.timing.MorseTiming
import kotlin.math.roundToInt
import kotlin.time.DurationUnit

/** One tone, as a half-open frame range `[startFrame, endFrame)`. */
data class ToneSegment(val startFrame: Int, val endFrame: Int) {
    val frameCount: Int get() = endFrame - startFrame
}

/** Where the tones fall in a transmission of [totalFrames] audio frames. */
data class ToneSchedule(val tones: List<ToneSegment>, val totalFrames: Int)

/**
 * Converts on/off [signals] into frame positions at [sampleRate].
 *
 * Every boundary is computed from the *cumulative* unit count, so rounding to whole frames never
 * accumulates: the error at any boundary is at most half a frame, however long the message.
 */
fun scheduleTones(signals: List<MorseSignal>, timing: MorseTiming, sampleRate: Int): ToneSchedule {
    val framesPerUnit = timing.unit.toDouble(DurationUnit.SECONDS) * sampleRate
    fun frameAt(units: Int) = (units * framesPerUnit).roundToInt()

    val tones = mutableListOf<ToneSegment>()
    var elapsedUnits = 0
    for (signal in signals) {
        val start = frameAt(elapsedUnits)
        elapsedUnits += signal.units
        if (signal.isOn) tones += ToneSegment(start, frameAt(elapsedUnits))
    }
    return ToneSchedule(tones, frameAt(elapsedUnits))
}
