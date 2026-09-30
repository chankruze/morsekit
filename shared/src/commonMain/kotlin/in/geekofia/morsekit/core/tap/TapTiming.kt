package `in`.geekofia.morsekit.core.tap

import `in`.geekofia.morsekit.core.timing.MorseTiming

/**
 * Thresholds for keying Morse by hand, from a tap speed. With one unit = `1200 ms / wpm`:
 *
 * - a press of [DASH_UNITS] or more is a dash (a dot is 1 unit, a dash 3: 2 is the midpoint);
 * - a pause of [LETTER_GAP_UNITS] ends the letter (the standard letter gap, generous for fingers
 *   that hesitate between the elements of a letter);
 * - a pause of [WORD_GAP_UNITS] ends the word.
 *
 * At the default 8 WPM (150 ms units): dash from 300 ms, letter after 450 ms, word after 1050 ms.
 */
data class TapTiming(val wordsPerMinute: Int = DEFAULT_WPM) {
    private val unitMillis: Long = MorseTiming(wordsPerMinute).unit.inWholeMilliseconds

    val dashThresholdMillis: Long get() = unitMillis * DASH_UNITS
    val letterGapMillis: Long get() = unitMillis * LETTER_GAP_UNITS
    val wordGapMillis: Long get() = unitMillis * WORD_GAP_UNITS

    companion object {
        /** Slower than playback: tapping a screen is slower than keying, especially when learning. */
        const val DEFAULT_WPM = 8
        const val DASH_UNITS = 2
        const val LETTER_GAP_UNITS = 3
        const val WORD_GAP_UNITS = 7
    }
}
