package `in`.geekofia.morsekit.feature.tap

import androidx.lifecycle.ViewModel
import `in`.geekofia.morsekit.core.settings.SettingsRepository
import `in`.geekofia.morsekit.core.tap.TapMode
import `in`.geekofia.morsekit.core.tap.TapState
import `in`.geekofia.morsekit.core.tap.TapTiming

/**
 * The Tap screen: a [Keyer] with the saved tap speed and mode. [clock] is monotonic milliseconds;
 * tests pass fake time.
 */
class TapViewModel(
    private val settingsRepository: SettingsRepository,
    clock: () -> Long = monotonicMillis(),
) : ViewModel() {

    private val keyer = Keyer(timing = { timing }, clock = clock)

    val state: TapState get() = keyer.state

    val heldAsDash: Boolean get() = keyer.heldAsDash

    val preview: Char? get() = keyer.preview

    val timing: TapTiming get() = TapTiming(settingsRepository.settings.value.tapWordsPerMinute)

    val mode: TapMode get() = settingsRepository.settings.value.tapMode

    fun press() = keyer.press()

    fun release() = keyer.release()

    fun advance() = keyer.advance()

    fun millisUntilNextChange(): Long? = keyer.millisUntilNextChange()

    fun dot() = keyer.dot()

    fun dash() = keyer.dash()

    fun endLetter() = keyer.endLetter()

    fun space() = keyer.space()

    fun backspace() = keyer.backspace()

    fun clear() = keyer.clear()

    fun setTapSpeed(wordsPerMinute: Int) = settingsRepository.setTapWordsPerMinute(wordsPerMinute)

    /** Keeps what's been keyed, but no timer from one mode fires in the other. */
    fun setMode(mode: TapMode) {
        settingsRepository.setTapMode(mode)
        keyer.stopTimers()
    }
}
