package `in`.geekofia.morsekit.core.timing

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Standard ("PARIS") Morse timing. One unit is `1200 ms / wpm`:
 * dot = 1, dash = 3, gap inside a letter = 1, between letters = 3, between words = 7.
 */
data class MorseTiming(val wordsPerMinute: Int = DEFAULT_WPM) {
    init {
        require(wordsPerMinute in MIN_WPM..MAX_WPM) { "WPM must be in $MIN_WPM..$MAX_WPM, was $wordsPerMinute" }
    }

    val unit: Duration get() = (MILLIS_PER_UNIT_AT_1_WPM / wordsPerMinute).milliseconds

    fun durationOf(signal: MorseSignal): Duration = unit * signal.units

    fun totalDuration(signals: List<MorseSignal>): Duration = unit * signals.sumOf { it.units }

    companion object {
        const val DEFAULT_WPM = 20
        const val MIN_WPM = 5
        const val MAX_WPM = 60
        private const val MILLIS_PER_UNIT_AT_1_WPM = 1200.0
    }
}
