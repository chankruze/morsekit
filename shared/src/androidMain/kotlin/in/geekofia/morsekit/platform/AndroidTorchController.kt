package `in`.geekofia.morsekit.platform

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

/**
 * Uses [CameraManager.setTorchMode], which needs no camera permission. Android also turns the
 * torch off itself if this app's process dies while it's on.
 */
internal class AndroidTorchController(context: Context) : TorchController {
    private val cameraManager: CameraManager? = context.getSystemService(CameraManager::class.java)

    /** The back camera with a flash unit if there is one, otherwise any camera with one. */
    private val torchCameraId: String? by lazy {
        val manager = cameraManager ?: return@lazy null
        try {
            val withFlash = manager.cameraIdList.filter { id ->
                manager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            withFlash.firstOrNull { id ->
                manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) ==
                    CameraCharacteristics.LENS_FACING_BACK
            } ?: withFlash.firstOrNull()
        } catch (e: CameraAccessException) {
            null
        }
    }

    override val isAvailable: Boolean get() = torchCameraId != null

    override fun setTorch(on: Boolean): Boolean {
        val manager = cameraManager ?: return false
        val id = torchCameraId ?: return false
        return try {
            manager.setTorchMode(id, on)
            true
        } catch (e: CameraAccessException) {
            false // e.g. the camera is in use by another app
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}
