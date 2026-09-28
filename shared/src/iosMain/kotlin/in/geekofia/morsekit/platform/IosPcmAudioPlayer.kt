package `in`.geekofia.morsekit.platform

import `in`.geekofia.morsekit.core.audio.PcmAudio
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.set
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioFormat
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioPCMFormatFloat32
import platform.AVFAudio.AVAudioPlayerNode
import platform.AVFAudio.AVAudioPlayerNodeCompletionDataPlayedBack
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * Plays float PCM through [AVAudioEngine]: one [AVAudioPCMBuffer] scheduled on an
 * [AVAudioPlayerNode]. The "data played back" completion is dispatched to the main queue and
 * ignored if a newer [play] or a [stop] happened in the meantime.
 */
@OptIn(ExperimentalForeignApi::class)
internal class IosPcmAudioPlayer : PcmAudioPlayer {
    private val engine = AVAudioEngine()
    private val node = AVAudioPlayerNode()
    private var session = 0

    init {
        engine.attachNode(node)
    }

    override fun play(audio: PcmAudio, onComplete: () -> Unit) {
        stop()
        val format = AVAudioFormat(
            commonFormat = AVAudioPCMFormatFloat32,
            sampleRate = audio.sampleRate.toDouble(),
            channels = 1u,
            interleaved = false,
        )
        val buffer = AVAudioPCMBuffer(pCMFormat = format, frameCapacity = audio.frameCount.toUInt())
        buffer.frameLength = audio.frameCount.toUInt()
        val channel = buffer.floatChannelData?.get(0) ?: return
        audio.samples.forEachIndexed { index, sample -> channel[index] = sample }

        // Playback category: the user explicitly pressed Play, so play even with the silent switch on.
        AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, error = null)
        AVAudioSession.sharedInstance().setActive(true, error = null)
        engine.connect(node, to = engine.mainMixerNode, format = format)
        if (!engine.startAndReturnError(null)) return

        val current = ++session
        node.scheduleBuffer(
            buffer,
            atTime = null,
            options = 0u,
            completionCallbackType = AVAudioPlayerNodeCompletionDataPlayedBack,
        ) { _ ->
            dispatch_async(dispatch_get_main_queue()) {
                if (current == session) {
                    session++
                    onComplete()
                }
            }
        }
        node.play()
    }

    override fun stop() {
        session++
        node.stop()
        engine.stop()
    }
}
