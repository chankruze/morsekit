package `in`.geekofia.morsekit.core.settings

import `in`.geekofia.morsekit.core.timing.MorseTiming
import `in`.geekofia.morsekit.platform.InMemoryKeyValueStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SettingsRepositoryTest {
    private val store = InMemoryKeyValueStore()

    private fun repository() = SettingsRepository(store)

    @Test
    fun emptyStoreGivesDefaults() {
        assertEquals(AppSettings(), repository().settings.value)
        assertEquals(ThemeMode.System, AppSettings().themeMode)
        assertEquals(MorseTiming.DEFAULT_WPM, AppSettings().wordsPerMinute)
        assertEquals(AppSettings.DEFAULT_TONE_HZ, AppSettings().toneFrequencyHz)
    }

    @Test
    fun changesAreVisibleImmediately() {
        val repository = repository()
        repository.setThemeMode(ThemeMode.Dark)
        repository.setWordsPerMinute(25)
        repository.setToneFrequencyHz(700)
        assertEquals(AppSettings(ThemeMode.Dark, 25, 700), repository.settings.value)
    }

    @Test
    fun changesPersistAcrossInstances() {
        repository().apply {
            setThemeMode(ThemeMode.Light)
            setWordsPerMinute(12)
            setToneFrequencyHz(800)
        }
        assertEquals(AppSettings(ThemeMode.Light, 12, 800), repository().settings.value)
    }

    @Test
    fun changingOneSettingKeepsTheOthers() {
        val repository = repository()
        repository.setWordsPerMinute(30)
        repository.setThemeMode(ThemeMode.Dark)
        assertEquals(30, repository.settings.value.wordsPerMinute)
        assertEquals(AppSettings.DEFAULT_TONE_HZ, repository.settings.value.toneFrequencyHz)
    }

    @Test
    fun outOfRangeValuesAreClamped() {
        val repository = repository()
        repository.setWordsPerMinute(0)
        repository.setToneFrequencyHz(5_000)
        assertEquals(MorseTiming.MIN_WPM, repository.settings.value.wordsPerMinute)
        assertEquals(AppSettings.MAX_TONE_HZ, repository.settings.value.toneFrequencyHz)

        repository.setWordsPerMinute(999)
        repository.setToneFrequencyHz(-1)
        assertEquals(MorseTiming.MAX_WPM, repository.settings.value.wordsPerMinute)
        assertEquals(AppSettings.MIN_TONE_HZ, repository.settings.value.toneFrequencyHz)
    }

    @Test
    fun corruptStoredValuesFallBackSafely() {
        store.putString(SettingsRepository.KEY_THEME_MODE, "Purple")
        store.putInt(SettingsRepository.KEY_WPM, 500)
        store.putInt(SettingsRepository.KEY_TONE_HZ, 10)
        assertEquals(
            AppSettings(ThemeMode.System, MorseTiming.MAX_WPM, AppSettings.MIN_TONE_HZ),
            repository().settings.value,
        )
    }

    @Test
    fun flashWarningIsUnacknowledgedUntilAcceptedAndThenRemembered() {
        assertEquals(false, repository().settings.value.flashWarningAcknowledged)
        repository().acknowledgeFlashWarning()
        assertEquals(true, repository().settings.value.flashWarningAcknowledged)
    }

    @Test
    fun acknowledgingTheWarningKeepsOtherSettings() {
        val repository = repository()
        repository.setWordsPerMinute(30)
        repository.acknowledgeFlashWarning()
        assertEquals(AppSettings(wordsPerMinute = 30, flashWarningAcknowledged = true), repository.settings.value)
    }

    @Test
    fun appSettingsRejectsOutOfRangeValues() {
        assertFailsWith<IllegalArgumentException> { AppSettings(wordsPerMinute = 0) }
        assertFailsWith<IllegalArgumentException> { AppSettings(toneFrequencyHz = 20_000) }
    }
}
