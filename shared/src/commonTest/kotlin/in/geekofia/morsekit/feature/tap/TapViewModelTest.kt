package `in`.geekofia.morsekit.feature.tap

import `in`.geekofia.morsekit.core.settings.SettingsRepository
import `in`.geekofia.morsekit.core.tap.TapMode
import `in`.geekofia.morsekit.core.tap.TapTiming
import `in`.geekofia.morsekit.platform.InMemoryKeyValueStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TapViewModelTest {
    private var now = 0L
    private val settings = SettingsRepository(InMemoryKeyValueStore())
    private val viewModel = TapViewModel(settings, clock = { now })

    private fun hold(millis: Long) {
        viewModel.press()
        now += millis
        viewModel.release()
    }

    @Test
    fun usesTheSavedTapSpeed() {
        assertEquals(TapTiming.DEFAULT_WPM, viewModel.timing.wordsPerMinute)
        viewModel.setTapSpeed(12)
        assertEquals(12, settings.settings.value.tapWordsPerMinute)
        assertEquals(TapTiming(12), viewModel.timing)
    }

    @Test
    fun keysALetterWithTheClock() {
        hold(100)
        now += 150
        hold(400)
        assertEquals(".-", viewModel.state.currentLetter)
        assertEquals(450, viewModel.millisUntilNextChange())
        now += 450
        viewModel.advance()
        assertEquals("A", viewModel.state.text)
    }

    @Test
    fun heldAsDashTurnsOnAtTheThresholdAndOffOnRelease() {
        viewModel.press()
        assertEquals(300, viewModel.millisUntilNextChange())
        now += 299
        viewModel.advance()
        assertFalse(viewModel.heldAsDash)
        now += 1
        viewModel.advance()
        assertTrue(viewModel.heldAsDash)
        viewModel.release()
        assertFalse(viewModel.heldAsDash)
        assertEquals("-", viewModel.state.currentLetter)
    }

    @Test
    fun aFasterTapSpeedShortensTheThresholds() {
        viewModel.setTapSpeed(16) // 75 ms units: dash from 150 ms
        hold(200)
        assertEquals("-", viewModel.state.currentLetter)
    }

    @Test
    fun screenReaderActionsBuildTextWithoutTiming() {
        viewModel.dot()
        viewModel.dash()
        viewModel.endLetter()
        viewModel.space()
        viewModel.dash()
        viewModel.endLetter()
        assertEquals("A T", viewModel.state.text)
        assertNull(viewModel.millisUntilNextChange())
    }

    @Test
    fun backspaceAndClear() {
        viewModel.dot()
        viewModel.endLetter()
        viewModel.dash()
        viewModel.backspace()
        assertEquals("", viewModel.state.currentLetter)
        viewModel.clear()
        assertTrue(viewModel.state.isEmpty)
    }

    @Test
    fun switchingModesKeepsTheTextAndStopsTimers() {
        assertEquals(TapMode.Timing, viewModel.mode)
        hold(100) // "." with a letter gap pending
        assertEquals(450, viewModel.millisUntilNextChange())
        viewModel.setMode(TapMode.Buttons)
        assertEquals(TapMode.Buttons, settings.settings.value.tapMode)
        assertEquals(".", viewModel.state.currentLetter)
        assertNull(viewModel.millisUntilNextChange())
    }

    @Test
    fun buttonsModeKeysWithoutTimingAndPreviewsTheLetter() {
        viewModel.setMode(TapMode.Buttons)
        viewModel.dash()
        assertEquals('T', viewModel.preview)
        viewModel.dot()
        assertEquals('N', viewModel.preview)
        now += 60_000 // no timing: waiting changes nothing
        viewModel.advance()
        assertEquals("-.", viewModel.state.currentLetter)
        viewModel.endLetter()
        assertEquals("N", viewModel.state.text)
        assertNull(viewModel.preview)
    }
}
