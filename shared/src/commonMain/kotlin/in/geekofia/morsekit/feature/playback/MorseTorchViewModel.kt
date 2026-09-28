package `in`.geekofia.morsekit.feature.playback

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.geekofia.morsekit.core.model.MorseMessage
import `in`.geekofia.morsekit.core.settings.SettingsRepository
import `in`.geekofia.morsekit.core.timing.MorseTiming
import `in`.geekofia.morsekit.core.torch.TorchFailedException
import `in`.geekofia.morsekit.core.torch.TorchPlan
import `in`.geekofia.morsekit.core.torch.transmitWithTorch
import `in`.geekofia.morsekit.platform.TorchController

/**
 * Flashes a message at the saved speed. Thin by design: timing is in the tested [TorchPlan] and
 * [transmitWithTorch], and start/stop ordering in the tested [TransmissionRunner].
 */
class MorseTorchViewModel(
    private val torch: TorchController,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val runner = TransmissionRunner(viewModelScope)

    val isTorchAvailable: Boolean get() = torch.isAvailable

    val isTransmitting: Boolean get() = runner.isRunning

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun start(message: MorseMessage) {
        if (message.isEmpty) return
        if (!torch.isAvailable) {
            errorMessage = "This device doesn't have a flashlight."
            return
        }
        val timing = MorseTiming(settingsRepository.settings.value.wordsPerMinute)
        if (TorchPlan.isTooLong(message, timing)) {
            errorMessage = "Too long to flash: over ${TorchPlan.MAX_DURATION.inWholeMinutes} minutes at this speed."
            return
        }
        errorMessage = null
        runner.start {
            try {
                transmitWithTorch(TorchPlan.of(message, timing), torch)
            } catch (e: TorchFailedException) {
                errorMessage = "The flashlight stopped responding. Another app may be using the camera."
            }
        }
    }

    fun stop() = runner.stop()

    override fun onCleared() {
        runner.stop()
        torch.setTorch(false)
    }
}
