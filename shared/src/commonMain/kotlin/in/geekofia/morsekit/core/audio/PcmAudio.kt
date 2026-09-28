package `in`.geekofia.morsekit.core.audio

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Mono audio as 32-bit float samples in -1..1, ready for the platform to play as-is. */
class PcmAudio(val samples: FloatArray, val sampleRate: Int) {
    init {
        require(sampleRate > 0) { "Sample rate must be positive" }
    }

    val frameCount: Int get() = samples.size

    val duration: Duration get() = (frameCount.toDouble() / sampleRate).seconds
}
