package `in`.geekofia.morsekit.feature.playback

import `in`.geekofia.morsekit.core.timing.MorseTiming

/** The ways MorseKit can send a message. Only one runs at a time. */
enum class TransmitOutput { Sound, Flash, Vibrate }

/**
 * Speeds the − / + stepper moves between. A fixed ladder beats ±1: going from 20 to 60 WPM takes
 * a few taps, not 40. 13 WPM is included as a common Morse test speed.
 */
val WPM_STEPS: List<Int> = listOf(5, 8, 10, 12, 13, 15, 18, 20, 25, 30, 35, 40, 45, 50, 60)

/**
 * The next speed up ([faster]) or down from [current] on [WPM_STEPS]. A value between steps
 * (e.g. 17 set elsewhere) moves to the nearest step in that direction; the ends stay put.
 */
fun stepWpm(current: Int, faster: Boolean): Int {
    val clamped = current.coerceIn(MorseTiming.MIN_WPM, MorseTiming.MAX_WPM)
    return if (faster) {
        WPM_STEPS.firstOrNull { it > clamped } ?: WPM_STEPS.last()
    } else {
        WPM_STEPS.lastOrNull { it < clamped } ?: WPM_STEPS.first()
    }
}
