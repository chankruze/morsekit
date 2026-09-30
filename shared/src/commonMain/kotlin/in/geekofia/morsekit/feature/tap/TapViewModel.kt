package `in`.geekofia.morsekit.feature.tap

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import `in`.geekofia.morsekit.core.settings.SettingsRepository
import `in`.geekofia.morsekit.core.tap.TapDecoder
import `in`.geekofia.morsekit.core.tap.TapMode
import `in`.geekofia.morsekit.core.tap.TapState
import `in`.geekofia.morsekit.core.tap.TapTiming
import kotlin.time.TimeSource

/**
 * Keying state for the Tap screen. Thin: [TapDecoder] makes every decision; this class adds the
 * clock and the saved tap speed. Snapshot state, like the translator, so the key reacts in the
 * same frame. [clock] is monotonic milliseconds; tests pass fake time.
 */
class TapViewModel(
    private val settingsRepository: SettingsRepository,
    private val clock: () -> Long = monotonicMillis(),
) : ViewModel() {

    var state by mutableStateOf(TapState())
        private set

    /** The key is held long enough to give a dash (for the key's colour and a haptic tick). */
    var heldAsDash by mutableStateOf(false)
        private set

    val timing: TapTiming get() = TapTiming(settingsRepository.settings.value.tapWordsPerMinute)

    val mode: TapMode get() = settingsRepository.settings.value.tapMode

    /** What the letter being keyed decodes to so far, e.g. `A` for `.-`. */
    val preview: Char? get() = decoder.preview(state)

    private val decoder: TapDecoder get() = TapDecoder(timing)

    fun press() {
        state = decoder.press(state, clock())
        heldAsDash = false
    }

    fun release() {
        state = decoder.release(state, clock())
        heldAsDash = false
    }

    /** Applies whatever is due now: a hold becoming a dash, a letter or word ending. */
    fun advance() {
        val now = clock()
        state = decoder.advance(state, now)
        heldAsDash = decoder.isDash(state, now)
    }

    /** Milliseconds until [advance] should run next, or `null` if nothing is pending. */
    fun millisUntilNextChange(): Long? = decoder.nextDeadline(state)?.let { (it - clock()).coerceAtLeast(0) }

    fun dot() = edit { decoder.element(it, dash = false) }

    fun dash() = edit { decoder.element(it, dash = true) }

    fun endLetter() = edit(decoder::endLetter)

    fun space() = edit(decoder::space)

    fun backspace() = edit(decoder::backspace)

    fun clear() = edit(decoder::clear)

    fun setTapSpeed(wordsPerMinute: Int) = settingsRepository.setTapWordsPerMinute(wordsPerMinute)

    /** Keeps what's been keyed, but no timer from one mode fires in the other. */
    fun setMode(mode: TapMode) {
        settingsRepository.setTapMode(mode)
        state = decoder.stopTimers(state)
        heldAsDash = false
    }

    private inline fun edit(transform: (TapState) -> TapState) {
        state = transform(state)
    }
}

private fun monotonicMillis(): () -> Long {
    val start = TimeSource.Monotonic.markNow()
    return { start.elapsedNow().inWholeMilliseconds }
}
