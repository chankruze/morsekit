package `in`.geekofia.morsekit.feature.playback

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.geekofia.morsekit.core.audio.MorseAudioPlayer
import `in`.geekofia.morsekit.core.audio.MorseAudioRenderer
import `in`.geekofia.morsekit.core.audio.PlaybackStatus
import `in`.geekofia.morsekit.core.model.MorseMessage
import `in`.geekofia.morsekit.core.settings.SettingsRepository
import `in`.geekofia.morsekit.core.timing.MorseTiming
import `in`.geekofia.morsekit.platform.PcmAudioPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Renders a message off the main thread and hands it to [MorseAudioPlayer]. Speed and tone come
 * from [SettingsRepository] at the moment Play is pressed.
 *
 * Deliberately thin: timing and rendering ([MorseAudioRenderer]) and the state machine
 * ([MorseAudioPlayer]) hold the logic and are unit-tested; this only orchestrates them.
 */
class MorsePlaybackViewModel(
    output: PcmAudioPlayer,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val player = MorseAudioPlayer(output)
    private var renderJob: Job? = null

    val status: StateFlow<PlaybackStatus> = player.status

    /** True while the tone is being rendered, before playback starts. */
    var isPreparing by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun play(message: MorseMessage) {
        stop()
        if (message.isEmpty) return
        val settings = settingsRepository.settings.value
        val timing = MorseTiming(settings.wordsPerMinute)
        if (MorseAudioRenderer.isTooLongToPlay(message, timing)) {
            errorMessage = "Too long to play: over ${MorseAudioRenderer.MAX_DURATION.inWholeMinutes} minutes at this speed."
            return
        }
        isPreparing = true
        renderJob = viewModelScope.launch {
            val audio = withContext(Dispatchers.Default) {
                MorseAudioRenderer.render(message, timing, settings.toneFrequencyHz)
            }
            isPreparing = false
            player.play(audio)
        }
    }

    fun stop() {
        renderJob?.cancel()
        renderJob = null
        isPreparing = false
        errorMessage = null
        player.stop()
    }

    override fun onCleared() = stop()
}
