package `in`.geekofia.morsekit.core.settings

import `in`.geekofia.morsekit.core.tap.TapMode
import `in`.geekofia.morsekit.core.tap.TapTiming
import `in`.geekofia.morsekit.core.timing.MorseTiming

/** User preferences. Values are always within range; [SettingsRepository] clamps anything it loads. */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    /** Default speed for audio, flashlight and vibration playback. */
    val wordsPerMinute: Int = MorseTiming.DEFAULT_WPM,
    /** Default pitch of the audio tone. */
    val toneFrequencyHz: Int = DEFAULT_TONE_HZ,
    /** The user has seen and accepted the flashing-light warning (asked once, before the first flash). */
    val flashWarningAcknowledged: Boolean = false,
    /** Speed for keying on the Tap screen; slower than playback by default (see [TapTiming]). */
    val tapWordsPerMinute: Int = TapTiming.DEFAULT_WPM,
    val tapMode: TapMode = TapMode.Timing,
) {
    init {
        require(wordsPerMinute in MorseTiming.MIN_WPM..MorseTiming.MAX_WPM) { "WPM out of range: $wordsPerMinute" }
        require(tapWordsPerMinute in MorseTiming.MIN_WPM..MorseTiming.MAX_WPM) { "Tap WPM out of range: $tapWordsPerMinute" }
        require(toneFrequencyHz in MIN_TONE_HZ..MAX_TONE_HZ) { "Tone out of range: $toneFrequencyHz" }
    }

    companion object {
        const val DEFAULT_TONE_HZ = 600
        const val MIN_TONE_HZ = 400
        const val MAX_TONE_HZ = 1000
        const val TONE_STEP_HZ = 50
    }
}
