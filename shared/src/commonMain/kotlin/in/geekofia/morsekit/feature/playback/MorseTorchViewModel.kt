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
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

/**
 * Runs [transmitWithTorch] for a message at the saved speed. Thin by design: timing is in the
 * tested [TorchPlan] and [transmitWithTorch]; this only starts, stops and reports.
 *
 * A new transmission waits for the previous one to finish (which turns the torch off) before
 * starting, so two transmissions can never overlap.
 */
class MorseTorchViewModel(
    private val torch: TorchController,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private var job: Job? = null

    val isTorchAvailable: Boolean get() = torch.isAvailable

    var isTransmitting by mutableStateOf(false)
        private set

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
        isTransmitting = true
        val previous = job
        // LAZY: viewModelScope runs on Main.immediate, so an eagerly started body could finish
        // (e.g. the torch fails at once) before `job` is assigned, and `finally` would miss it.
        val newJob = viewModelScope.launch(start = CoroutineStart.LAZY) {
            val self = coroutineContext[Job]
            previous?.cancelAndJoin()
            try {
                transmitWithTorch(TorchPlan.of(message, timing), torch)
            } catch (e: TorchFailedException) {
                errorMessage = "The flashlight stopped responding. Another app may be using the camera."
            } finally {
                if (job === self) {
                    job = null
                    isTransmitting = false
                }
            }
        }
        job = newJob
        newJob.start()
    }

    /** Safe to call at any time; the running transmission turns the torch off as it ends. */
    fun stop() {
        job?.cancel()
        job = null
        isTransmitting = false
    }

    override fun onCleared() {
        stop()
        torch.setTorch(false)
    }
}
