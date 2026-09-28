package `in`.geekofia.morsekit.platform

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import `in`.geekofia.morsekit.core.audio.PcmAudio
import kotlin.concurrent.thread

/**
 * Streams float PCM to an [AudioTrack]. A background thread writes the samples (blocking writes
 * naturally wait while paused); a notification marker on the last frame reports completion on the
 * main thread. A new track is created per [play], so stale callbacks can't reach a newer one.
 */
internal class AndroidPcmAudioPlayer : PcmAudioPlayer {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var track: AudioTrack? = null
    private var writer: Thread? = null

    override fun play(audio: PcmAudio, onComplete: () -> Unit) {
        stop()
        val newTrack = createTrack(audio.sampleRate)
        track = newTrack

        newTrack.notificationMarkerPosition = audio.frameCount
        newTrack.setPlaybackPositionUpdateListener(
            object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(reached: AudioTrack) {
                    if (reached !== track) return
                    release()
                    onComplete()
                }

                override fun onPeriodicNotification(track: AudioTrack) = Unit
            },
            mainHandler,
        )
        newTrack.play()

        val samples = audio.samples
        writer = thread(name = "MorseKit-audio") {
            var offset = 0
            while (offset < samples.size) {
                val written = newTrack.write(
                    samples, offset, minOf(WRITE_CHUNK_FRAMES, samples.size - offset), AudioTrack.WRITE_BLOCKING,
                )
                if (written <= 0) break // stopped/released (returns 0 or an error code)
                offset += written
            }
        }
    }

    override fun pause() {
        track?.pause()
    }

    override fun resume() {
        track?.play()
    }

    override fun stop() {
        val current = track ?: return
        track = null
        current.pause()
        current.flush()
        current.stop()
        writer?.join(WRITER_JOIN_TIMEOUT_MS)
        writer = null
        current.release()
    }

    /** Called when playback finished normally; the writer has already exited. */
    private fun release() {
        track?.release()
        track = null
        writer = null
    }

    private fun createTrack(sampleRate: Int): AudioTrack {
        val format = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        val minBuffer = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT)
        return AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .setAudioFormat(format)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setBufferSizeInBytes(maxOf(minBuffer, sampleRate * Float.SIZE_BYTES / 4)) // ~250 ms
            .build()
    }

    private companion object {
        const val WRITE_CHUNK_FRAMES = 4_096
        const val WRITER_JOIN_TIMEOUT_MS = 500L
    }
}
