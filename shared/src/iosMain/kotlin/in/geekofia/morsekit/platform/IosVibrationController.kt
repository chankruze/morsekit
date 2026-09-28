package `in`.geekofia.morsekit.platform

import `in`.geekofia.morsekit.core.vibration.VibrationPattern
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreHaptics.CHHapticEngine
import platform.CoreHaptics.CHHapticEvent
import platform.CoreHaptics.CHHapticEventParameter
import platform.CoreHaptics.CHHapticEventParameterIDHapticIntensity
import platform.CoreHaptics.CHHapticEventParameterIDHapticSharpness
import platform.CoreHaptics.CHHapticEventTypeHapticContinuous
import platform.CoreHaptics.CHHapticPattern
import platform.CoreHaptics.CHHapticPatternPlayerProtocol

/**
 * Plays the pattern with Core Haptics: one continuous haptic event per "on" segment, at its
 * offset in a single [CHHapticPattern], so the Taptic Engine does the timing.
 * iOS stops the haptic engine in the background; it's restarted on every [vibrate].
 */
@OptIn(ExperimentalForeignApi::class)
internal class IosVibrationController : VibrationController {
    private var engine: CHHapticEngine? = null
    private var player: CHHapticPatternPlayerProtocol? = null

    override val isAvailable: Boolean
        get() = CHHapticEngine.capabilitiesForHardware().supportsHaptics

    override fun vibrate(pattern: VibrationPattern): Boolean {
        cancel()
        if (!isAvailable) return false
        val engine = engine ?: CHHapticEngine(null).also { engine = it }
        if (!engine.startAndReturnError(null)) return false

        val events = mutableListOf<CHHapticEvent>()
        var offsetMillis = 0L
        for (segment in pattern.segments) {
            if (segment.isOn) {
                events += CHHapticEvent(
                    eventType = CHHapticEventTypeHapticContinuous,
                    parameters = listOf(
                        CHHapticEventParameter(CHHapticEventParameterIDHapticIntensity, 1.0f),
                        CHHapticEventParameter(CHHapticEventParameterIDHapticSharpness, 0.5f),
                    ),
                    relativeTime = offsetMillis / 1000.0,
                    duration = segment.durationMillis / 1000.0,
                )
            }
            offsetMillis += segment.durationMillis
        }

        val hapticPattern = CHHapticPattern(events = events, parameters = emptyList<Any>(), error = null)
        val newPlayer = engine.createPlayerWithPattern(hapticPattern, null) ?: return false
        if (!newPlayer.startAtTime(IMMEDIATELY, null)) return false
        player = newPlayer
        return true
    }

    override fun cancel() {
        player?.stopAtTime(IMMEDIATELY, null)
        player = null
    }

    private companion object {
        /** `CHHapticTimeImmediate`. */
        const val IMMEDIATELY = 0.0
    }
}
