package `in`.geekofia.morsekit.core.audio

import `in`.geekofia.morsekit.platform.PcmAudioPlayer
import kotlin.test.Test
import kotlin.test.assertEquals

/** Records calls and lets the test decide when playback "finishes". */
private class FakePcmAudioPlayer : PcmAudioPlayer {
    val calls = mutableListOf<String>()
    val completions = mutableListOf<() -> Unit>()

    override fun play(audio: PcmAudio, onComplete: () -> Unit) {
        calls += "play"
        completions += onComplete
    }

    override fun pause() { calls += "pause" }
    override fun resume() { calls += "resume" }
    override fun stop() { calls += "stop" }
}

class MorseAudioPlayerTest {
    private val output = FakePcmAudioPlayer()
    private val player = MorseAudioPlayer(output)
    private val audio = PcmAudio(FloatArray(10), 16_000)

    @Test
    fun startsIdle() {
        assertEquals(PlaybackStatus.Idle, player.status.value)
    }

    @Test
    fun playPauseResumeStop() {
        player.play(audio)
        assertEquals(PlaybackStatus.Playing, player.status.value)
        player.pause()
        assertEquals(PlaybackStatus.Paused, player.status.value)
        player.resume()
        assertEquals(PlaybackStatus.Playing, player.status.value)
        player.stop()
        assertEquals(PlaybackStatus.Idle, player.status.value)
        assertEquals(listOf("play", "pause", "resume", "stop"), output.calls)
    }

    @Test
    fun completionReturnsToIdle() {
        player.play(audio)
        output.completions.single().invoke()
        assertEquals(PlaybackStatus.Idle, player.status.value)
    }

    @Test
    fun canReplayAfterCompletion() {
        player.play(audio)
        output.completions.single().invoke()
        player.play(audio)
        assertEquals(PlaybackStatus.Playing, player.status.value)
        assertEquals(listOf("play", "play"), output.calls)
    }

    @Test
    fun invalidTransitionsAreIgnored() {
        player.pause()
        player.resume()
        player.stop()
        assertEquals(PlaybackStatus.Idle, player.status.value)
        assertEquals(emptyList(), output.calls)

        player.play(audio)
        player.resume() // not paused
        assertEquals(listOf("play"), output.calls)
    }

    @Test
    fun playWhilePlayingStopsTheCurrentAudioFirst() {
        player.play(audio)
        player.play(audio)
        assertEquals(listOf("play", "stop", "play"), output.calls)
        assertEquals(PlaybackStatus.Playing, player.status.value)
    }

    @Test
    fun lateCompletionFromEarlierPlaybackIsIgnored() {
        player.play(audio)
        val staleCompletion = output.completions.first()
        player.play(audio)
        staleCompletion()
        assertEquals(PlaybackStatus.Playing, player.status.value)
    }

    @Test
    fun completionAfterStopIsIgnored() {
        player.play(audio)
        player.stop()
        player.play(audio)
        player.pause()
        output.completions.first().invoke()
        assertEquals(PlaybackStatus.Paused, player.status.value)
    }
}
