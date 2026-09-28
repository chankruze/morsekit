package `in`.geekofia.morsekit.platform

import `in`.geekofia.morsekit.core.audio.PcmAudio

/**
 * Plays rendered [PcmAudio] with the platform's native audio API (AudioTrack / AVAudioEngine).
 *
 * It only plays samples; all Morse timing is already in the audio. Threading contract: every
 * method is called on the main thread, and implementations invoke `onComplete` on the main
 * thread, exactly once, and only if playback reached the end (not after [stop] or a new [play]).
 */
interface PcmAudioPlayer {
    /** Starts [audio] from the beginning, replacing anything already playing. */
    fun play(audio: PcmAudio, onComplete: () -> Unit)

    fun pause()

    fun resume()

    fun stop()
}

/** Plays nothing and never completes. For previews. */
class NoOpPcmAudioPlayer : PcmAudioPlayer {
    override fun play(audio: PcmAudio, onComplete: () -> Unit) = Unit
    override fun pause() = Unit
    override fun resume() = Unit
    override fun stop() = Unit
}
