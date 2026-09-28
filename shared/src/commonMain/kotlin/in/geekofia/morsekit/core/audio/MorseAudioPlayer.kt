package `in`.geekofia.morsekit.core.audio

import `in`.geekofia.morsekit.platform.PcmAudioPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PlaybackStatus { Idle, Playing }

/**
 * The play / stop state machine on top of a platform [PcmAudioPlayer].
 *
 * Synchronous and main-thread only, like the [PcmAudioPlayer] contract, so it's fully testable
 * with a fake output. Stopping while idle is ignored.
 *
 * There's deliberately no pause: on a real device, resuming a paused `AudioTrack` came back
 * silent, so Sound works like the flashlight and vibration (start / stop).
 */
class MorseAudioPlayer(private val output: PcmAudioPlayer) {

    private val state = MutableStateFlow(PlaybackStatus.Idle)
    val status: StateFlow<PlaybackStatus> = state.asStateFlow()

    /** Identifies the current playback, so a late completion from an earlier one is ignored. */
    private var session = 0

    fun play(audio: PcmAudio) {
        if (state.value != PlaybackStatus.Idle) output.stop()
        val current = ++session
        state.value = PlaybackStatus.Playing
        output.play(audio) {
            if (current == session) state.value = PlaybackStatus.Idle
        }
    }

    fun stop() {
        if (state.value == PlaybackStatus.Idle) return
        session++
        output.stop()
        state.value = PlaybackStatus.Idle
    }
}
