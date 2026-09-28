package `in`.geekofia.morsekit.platform

/**
 * Switches the device flashlight. Implementations only switch it; all Morse timing is shared.
 * Called on the main thread.
 */
interface TorchController {
    /** False on devices without a controllable flashlight (e.g. most tablets and emulators). */
    val isAvailable: Boolean

    /** Returns false if the torch couldn't be switched, e.g. another app is using the camera. */
    fun setTorch(on: Boolean): Boolean
}

/** A device without a flashlight. For previews and as a safe fallback. */
class NoTorchController : TorchController {
    override val isAvailable: Boolean = false
    override fun setTorch(on: Boolean): Boolean = false
}
