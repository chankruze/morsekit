package `in`.geekofia.morsekit.platform

import `in`.geekofia.morsekit.core.update.AvailableUpdate
import `in`.geekofia.morsekit.core.update.UpdateMode

/**
 * The store's in-app update flow. Callbacks arrive on the main thread.
 * Android: Google Play In-App Updates. iOS: none (see [NoAppUpdateService]).
 */
interface AppUpdateService {
    /** False where the platform has no in-app update flow; update UI is hidden then. */
    val isSupported: Boolean

    /**
     * Asks the store about a newer version: [onResult] gets it, or null. [manual] is true for the
     * user's "Check for updates" (debug builds use it to simulate an available update).
     */
    fun checkForUpdate(manual: Boolean, onResult: (AvailableUpdate?) -> Unit)

    /** Starts the store's update UI. [onResult] is true if the user accepted. */
    fun startUpdate(mode: UpdateMode, onResult: (accepted: Boolean) -> Unit)

    /** Called whenever a flexible update has finished downloading and is ready to install. */
    fun setOnReadyToInstall(listener: () -> Unit)

    /** Installs a downloaded flexible update (the app restarts). */
    fun completeUpdate()
}

/**
 * No in-app updates. Used on iOS, where Apple offers no in-app update API and MorseKit doesn't
 * query the App Store itself (it stays offline; iOS's automatic App Store updates apply), and
 * in previews.
 */
class NoAppUpdateService : AppUpdateService {
    override val isSupported: Boolean = false
    override fun checkForUpdate(manual: Boolean, onResult: (AvailableUpdate?) -> Unit) = onResult(null)
    override fun startUpdate(mode: UpdateMode, onResult: (accepted: Boolean) -> Unit) = onResult(false)
    override fun setOnReadyToInstall(listener: () -> Unit) = Unit
    override fun completeUpdate() = Unit
}
