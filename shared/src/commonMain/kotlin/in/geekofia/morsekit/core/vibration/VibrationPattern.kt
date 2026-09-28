package `in`.geekofia.morsekit.core.vibration

import `in`.geekofia.morsekit.core.model.MorseMessage
import `in`.geekofia.morsekit.core.timing.MorseSignal
import `in`.geekofia.morsekit.core.timing.MorseTiming
import `in`.geekofia.morsekit.core.timing.toSignals
import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

/** One stretch of vibration ([isOn]) or stillness, in whole milliseconds. */
data class VibrationSegment(val isOn: Boolean, val durationMillis: Long) {
    init {
        require(durationMillis > 0) { "A segment must last at least 1 ms" }
    }
}

/**
 * A complete Morse transmission as alternating on/off segments, starting with "on", ready to hand
 * to a native vibration API in one call.
 *
 * Milliseconds because Android's waveform API takes whole milliseconds. Boundaries are rounded
 * from *cumulative* units, so rounding never accumulates over a long message.
 */
class VibrationPattern(val segments: List<VibrationSegment>) {
    val durationMillis: Long = segments.sumOf { it.durationMillis }

    val duration: Duration get() = durationMillis.milliseconds

    companion object {
        /** Longer messages aren't vibrated, to avoid long unattended buzzing. */
        val MAX_DURATION: Duration = 5.minutes

        fun of(message: MorseMessage, timing: MorseTiming): VibrationPattern = of(message.toSignals(), timing)

        fun of(signals: List<MorseSignal>, timing: MorseTiming): VibrationPattern {
            val unitMillis = timing.unit.toDouble(DurationUnit.MILLISECONDS)
            var units = 0
            var previousBoundary = 0L
            val segments = signals.map { signal ->
                units += signal.units
                val boundary = (units * unitMillis).roundToLong()
                VibrationSegment(signal.isOn, boundary - previousBoundary).also { previousBoundary = boundary }
            }
            return VibrationPattern(segments)
        }

        fun isTooLong(message: MorseMessage, timing: MorseTiming): Boolean =
            timing.totalDuration(message.toSignals()) > MAX_DURATION
    }
}
