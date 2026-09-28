package `in`.geekofia.morsekit.core.torch

import `in`.geekofia.morsekit.core.model.MorseMessage
import `in`.geekofia.morsekit.core.timing.MorseSignal
import `in`.geekofia.morsekit.core.timing.MorseTiming
import `in`.geekofia.morsekit.core.timing.toSignals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/** What the torch should be doing at some moment. [nextChangeIn] is `null` once the message is over. */
data class TorchStep(val isOn: Boolean, val nextChangeIn: Duration?)

/**
 * When the torch should be on or off, as a pure function of time since the start.
 *
 * Signal boundaries are computed from cumulative units, so they don't drift; and because the
 * runner asks "what now?" rather than chaining delays, late wake-ups don't accumulate either.
 */
class TorchPlan(signals: List<MorseSignal>, timing: MorseTiming) {
    private val states: List<Boolean> = signals.map { it.isOn }
    private val ends: List<Duration>

    val duration: Duration

    init {
        var units = 0
        ends = signals.map { signal ->
            units += signal.units
            timing.unit * units
        }
        duration = timing.unit * units
    }

    fun stepAt(elapsed: Duration): TorchStep {
        require(!elapsed.isNegative()) { "Elapsed time can't be negative" }
        if (elapsed >= duration) return TorchStep(isOn = false, nextChangeIn = null)
        val index = firstEndAfter(elapsed)
        return TorchStep(isOn = states[index], nextChangeIn = ends[index] - elapsed)
    }

    /** Binary search for the signal in progress at [elapsed]: the first one ending after it. */
    private fun firstEndAfter(elapsed: Duration): Int {
        var low = 0
        var high = ends.lastIndex
        while (low < high) {
            val mid = (low + high) / 2
            if (ends[mid] > elapsed) high = mid else low = mid + 1
        }
        return low
    }

    companion object {
        /** Longer messages aren't flashed, to limit continuous flashing and heat. */
        val MAX_DURATION: Duration = 5.minutes

        fun of(message: MorseMessage, timing: MorseTiming) = TorchPlan(message.toSignals(), timing)

        fun isTooLong(message: MorseMessage, timing: MorseTiming): Boolean =
            timing.totalDuration(message.toSignals()) > MAX_DURATION
    }
}
