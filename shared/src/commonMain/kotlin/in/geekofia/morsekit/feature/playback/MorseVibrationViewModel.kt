package `in`.geekofia.morsekit.feature.playback

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.geekofia.morsekit.core.model.MorseMessage
import `in`.geekofia.morsekit.core.settings.SettingsRepository
import `in`.geekofia.morsekit.core.timing.MorseTiming
import `in`.geekofia.morsekit.core.vibration.VibrationFailedException
import `in`.geekofia.morsekit.core.vibration.VibrationPattern
import `in`.geekofia.morsekit.core.vibration.transmitWithVibration
import `in`.geekofia.morsekit.platform.VibrationController

/**
 * Vibrates a message at the saved speed. Thin by design, like [MorseTorchViewModel]: timing is in
 * the tested [VibrationPattern] and [transmitWithVibration], ordering in [TransmissionRunner].
 */
class MorseVibrationViewModel(
    private val vibration: VibrationController,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val runner = TransmissionRunner(viewModelScope)

    val isVibrationAvailable: Boolean get() = vibration.isAvailable

    val isTransmitting: Boolean get() = runner.isRunning

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun start(message: MorseMessage) {
        if (message.isEmpty) return
        if (!vibration.isAvailable) {
            errorMessage = "This device can't vibrate."
            return
        }
        val timing = MorseTiming(settingsRepository.settings.value.wordsPerMinute)
        if (VibrationPattern.isTooLong(message, timing)) {
            errorMessage = "Too long to vibrate: over ${VibrationPattern.MAX_DURATION.inWholeMinutes} minutes at this speed."
            return
        }
        errorMessage = null
        runner.start {
            try {
                transmitWithVibration(VibrationPattern.of(message, timing), vibration)
            } catch (e: VibrationFailedException) {
                errorMessage = "Vibration couldn't be started."
            }
        }
    }

    fun stop() = runner.stop()

    override fun onCleared() {
        runner.stop()
        vibration.cancel()
    }
}
