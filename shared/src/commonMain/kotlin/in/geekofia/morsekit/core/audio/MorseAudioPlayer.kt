package `in`.geekofia.morsekit.core.audio

import `in`.geekofia.morsekit.platform.PcmAudioPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PlaybackStatus { Idle, Playing, Paused }

/**
 * The play / pause / resume / stop state machine on top of a platform [PcmAudioPlayer].
 *
 * Synchronous and main-thread only, like the [PcmAudioPlayer] contract, so it's fully testable
 * with a fake output. Invalid transitions (e.g. pause while idle) are ignored.
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

    fun pause() {
        if (state.value != PlaybackStatus.Playing) return
        output.pause()
        state.value = PlaybackStatus.Paused
    }

    fun resume() {
        if (state.value != PlaybackStatus.Paused) return
        output.resume()
        state.value = PlaybackStatus.Playing
    }

    fun stop() {
        if (state.value == PlaybackStatus.Idle) return
        session++
        output.stop()
        state.value = PlaybackStatus.Idle
    }
}
