package `in`.geekofia.morsekit.navigation

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * "Press back again to exit": the first back on the start screen asks for confirmation, a second
 * back within [window] confirms. After [window], it starts over. [clock] is injectable for tests.
 */
class ExitConfirmation(
    private val window: Duration = DEFAULT_WINDOW,
    private val clock: TimeSource = TimeSource.Monotonic,
) {
    enum class Decision { ShowHint, Exit }

    private var firstPress: TimeMark? = null

    fun onBack(): Decision {
        val previous = firstPress
        if (previous != null && previous.elapsedNow() <= window) {
            firstPress = null
            return Decision.Exit
        }
        firstPress = clock.markNow()
        return Decision.ShowHint
    }

    companion object {
        /** Matches a short snackbar's display time, so the hint is visible for the whole window. */
        val DEFAULT_WINDOW: Duration = 2.seconds
    }
}
