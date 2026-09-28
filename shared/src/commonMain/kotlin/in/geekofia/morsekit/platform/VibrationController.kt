package `in`.geekofia.morsekit.platform

import `in`.geekofia.morsekit.core.vibration.VibrationPattern

/**
 * Plays a [VibrationPattern] on the device's vibration motor / Taptic Engine. The pattern already
 * contains all Morse timing; the platform plays it natively in one call. Called on the main thread.
 */
interface VibrationController {
    /** False on devices without vibration hardware (e.g. most tablets and emulators). */
    val isAvailable: Boolean

    /** Starts [pattern], replacing any running one. Returns false if it couldn't start. */
    fun vibrate(pattern: VibrationPattern): Boolean

    /** Stops any running pattern immediately. Safe to call when nothing is playing. */
    fun cancel()
}

/** A device without vibration hardware. For previews and as a safe fallback. */
class NoVibrationController : VibrationController {
    override val isAvailable: Boolean = false
    override fun vibrate(pattern: VibrationPattern): Boolean = false
    override fun cancel() = Unit
}
