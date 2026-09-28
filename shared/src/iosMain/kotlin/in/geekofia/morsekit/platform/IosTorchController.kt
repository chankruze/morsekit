package `in`.geekofia.morsekit.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureTorchModeOff
import platform.AVFoundation.AVCaptureTorchModeOn
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.hasTorch
import platform.AVFoundation.isTorchModeSupported
import platform.AVFoundation.torchAvailable
import platform.AVFoundation.torchMode

/**
 * Uses the default video [AVCaptureDevice]'s torch. `torchAvailable` becomes false when iOS
 * restricts the torch (e.g. overheating), which surfaces as a failed [setTorch].
 */
@OptIn(ExperimentalForeignApi::class)
internal class IosTorchController : TorchController {
    private val device: AVCaptureDevice? = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)

    override val isAvailable: Boolean
        get() = device?.let { it.hasTorch && it.torchAvailable } ?: false

    override fun setTorch(on: Boolean): Boolean {
        val device = device ?: return false
        val mode = if (on) AVCaptureTorchModeOn else AVCaptureTorchModeOff
        // Setting an unsupported or unavailable mode raises an Objective-C exception, so check first.
        if (!device.hasTorch || !device.isTorchModeSupported(mode)) return false
        if (on && !device.torchAvailable) return false
        if (!device.lockForConfiguration(null)) return false
        device.torchMode = mode
        device.unlockForConfiguration()
        return true
    }
}
