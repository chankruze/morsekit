package `in`.geekofia.morsekit.core.vibration

import `in`.geekofia.morsekit.platform.VibrationController
import kotlinx.coroutines.delay
import kotlin.time.Duration

/** The vibration pattern couldn't be started. */
class VibrationFailedException : Exception("Vibration couldn't be started")

/**
 * Plays [pattern] on [vibration] and suspends until it has finished, so callers know when the
 * transmission is over. The OS times the pattern; this only waits for its known duration.
 *
 * Vibration is cancelled if the transmission is cancelled or fails. After a normal finish it's
 * left alone: the pattern ends by itself, and the OS starts it a few ms after [VibrationController.vibrate]
 * returns, so cancelling "on time" would clip the last element. [sleep] is injectable for tests.
 *
 * @throws VibrationFailedException if the pattern couldn't be started.
 */
suspend fun transmitWithVibration(
    pattern: VibrationPattern,
    vibration: VibrationController,
    sleep: suspend (Duration) -> Unit = { delay(it) },
) {
    if (pattern.segments.isEmpty()) return
    var finished = false
    try {
        if (!vibration.vibrate(pattern)) throw VibrationFailedException()
        sleep(pattern.duration)
        finished = true
    } finally {
        if (!finished) vibration.cancel()
    }
}
