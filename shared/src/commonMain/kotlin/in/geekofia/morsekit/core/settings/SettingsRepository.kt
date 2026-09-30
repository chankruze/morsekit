package `in`.geekofia.morsekit.core.settings

import `in`.geekofia.morsekit.core.timing.MorseTiming
import `in`.geekofia.morsekit.platform.KeyValueStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet

/**
 * The single source of truth for [AppSettings]: loads them from [store], exposes them as a
 * [StateFlow], and writes every change back.
 *
 * Create one per app process (see `AppContainer`) so every screen observes the same instance.
 */
class SettingsRepository(private val store: KeyValueStore) {

    private val state = MutableStateFlow(load())

    val settings: StateFlow<AppSettings> = state.asStateFlow()

    fun setThemeMode(mode: ThemeMode) = update { it.copy(themeMode = mode) }

    /** Values outside [MorseTiming.MIN_WPM]..[MorseTiming.MAX_WPM] are clamped. */
    fun setWordsPerMinute(wpm: Int) = update { it.copy(wordsPerMinute = wpm.clampWpm()) }

    /** Values outside [AppSettings.MIN_TONE_HZ]..[AppSettings.MAX_TONE_HZ] are clamped. */
    fun setToneFrequencyHz(hz: Int) = update { it.copy(toneFrequencyHz = hz.clampTone()) }

    /** Clamped like [setWordsPerMinute]. */
    fun setTapWordsPerMinute(wpm: Int) = update { it.copy(tapWordsPerMinute = wpm.clampWpm()) }

    fun acknowledgeFlashWarning() = update { it.copy(flashWarningAcknowledged = true) }

    /**
     * Restores the user-facing settings (theme, speeds, tone) to their defaults. The flash-warning
     * acknowledgement isn't a setting shown on screen, so it's kept: resetting shouldn't make the
     * user accept the warning again.
     */
    fun resetToDefaults() = update { AppSettings(flashWarningAcknowledged = it.flashWarningAcknowledged) }

    private fun update(transform: (AppSettings) -> AppSettings) {
        val updated = state.updateAndGet(transform)
        store.putString(KEY_THEME_MODE, updated.themeMode.name)
        store.putInt(KEY_WPM, updated.wordsPerMinute)
        store.putInt(KEY_TONE_HZ, updated.toneFrequencyHz)
        store.putBoolean(KEY_FLASH_WARNING_ACKNOWLEDGED, updated.flashWarningAcknowledged)
        store.putInt(KEY_TAP_WPM, updated.tapWordsPerMinute)
    }

    /** Missing, unknown or out-of-range stored values fall back to defaults or are clamped. */
    private fun load(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            themeMode = store.getString(KEY_THEME_MODE)
                ?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
                ?: defaults.themeMode,
            wordsPerMinute = store.getInt(KEY_WPM)?.clampWpm() ?: defaults.wordsPerMinute,
            toneFrequencyHz = store.getInt(KEY_TONE_HZ)?.clampTone() ?: defaults.toneFrequencyHz,
            flashWarningAcknowledged = store.getBoolean(KEY_FLASH_WARNING_ACKNOWLEDGED)
                ?: defaults.flashWarningAcknowledged,
            tapWordsPerMinute = store.getInt(KEY_TAP_WPM)?.clampWpm() ?: defaults.tapWordsPerMinute,
        )
    }

    private fun Int.clampWpm() = coerceIn(MorseTiming.MIN_WPM, MorseTiming.MAX_WPM)

    private fun Int.clampTone() = coerceIn(AppSettings.MIN_TONE_HZ, AppSettings.MAX_TONE_HZ)

    internal companion object {
        const val KEY_THEME_MODE = "settings.themeMode"
        const val KEY_WPM = "settings.wordsPerMinute"
        const val KEY_TONE_HZ = "settings.toneFrequencyHz"
        const val KEY_FLASH_WARNING_ACKNOWLEDGED = "settings.flashWarningAcknowledged"
        const val KEY_TAP_WPM = "settings.tapWordsPerMinute"
    }
}
