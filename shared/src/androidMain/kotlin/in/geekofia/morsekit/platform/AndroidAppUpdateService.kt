package `in`.geekofia.morsekit.platform

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import `in`.geekofia.morsekit.core.update.AvailableUpdate
import `in`.geekofia.morsekit.core.update.UpdateMode

/**
 * Google Play In-App Updates. Works only for apps installed from Play; it talks to the Play Store
 * app, so MorseKit itself needs no internet permission.
 *
 * Debuggable builds use [FakeAppUpdateManager]. Automatic checks report no update; a manual
 * "Check for updates" simulates a newer version and walks it through accept -> download ->
 * downloaded, so the update UI can be tried before the app is on Play.
 */
internal class AndroidAppUpdateService(
    private val context: Context,
    private val currentActivity: () -> Activity?,
) : AppUpdateService {
    private val fake: FakeAppUpdateManager? =
        if (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) FakeAppUpdateManager(context) else null
    private val manager: AppUpdateManager = fake ?: AppUpdateManagerFactory.create(context)
    private var onReadyToInstall: () -> Unit = {}

    init {
        // App-scoped (lives as long as the process), so the listener is never unregistered.
        manager.registerListener(
            InstallStateUpdatedListener { state ->
                if (state.installStatus() == InstallStatus.DOWNLOADED) onReadyToInstall()
            },
        )
    }

    override val isSupported: Boolean = true

    override fun checkForUpdate(manual: Boolean, onResult: (AvailableUpdate?) -> Unit) {
        if (manual) fake?.setUpdateAvailable(installedVersionCode() + 1)
        manager.appUpdateInfo
            .addOnSuccessListener { info ->
                // A flexible update downloaded earlier (e.g. before the app was closed).
                if (info.installStatus() == InstallStatus.DOWNLOADED) onReadyToInstall()
                onResult(info.toAvailableUpdate())
            }
            .addOnFailureListener { onResult(null) }
    }

    override fun startUpdate(mode: UpdateMode, onResult: (accepted: Boolean) -> Unit) {
        val activity = currentActivity() ?: return onResult(false)
        val type = if (mode == UpdateMode.Immediate) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE
        manager.appUpdateInfo.addOnSuccessListener { info ->
            manager.startUpdateFlow(info, activity, AppUpdateOptions.defaultOptions(type))
                .addOnSuccessListener { resultCode -> onResult(resultCode == Activity.RESULT_OK) }
                .addOnFailureListener { onResult(false) }
            fake?.let { simulateAcceptedDownload(it, type) }
        }
    }

    override fun setOnReadyToInstall(listener: () -> Unit) {
        onReadyToInstall = listener
    }

    override fun completeUpdate() {
        manager.completeUpdate()
        fake?.installCompletes()
    }

    private fun AppUpdateInfo.toAvailableUpdate(): AvailableUpdate? {
        val availability = updateAvailability()
        if (availability != UpdateAvailability.UPDATE_AVAILABLE &&
            availability != UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
        ) return null
        return AvailableUpdate(
            versionCode = availableVersionCode(),
            priority = updatePriority(),
            stalenessDays = clientVersionStalenessDays(),
            flexibleAllowed = isUpdateTypeAllowed(AppUpdateType.FLEXIBLE),
            immediateAllowed = isUpdateTypeAllowed(AppUpdateType.IMMEDIATE),
            immediateInProgress = availability == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS,
        )
    }

    /** Debug only: plays the part of the user and Play for the fake manager. */
    private fun simulateAcceptedDownload(fake: FakeAppUpdateManager, type: Int) {
        fake.userAcceptsUpdate()
        fake.downloadStarts()
        fake.downloadCompletes()
        if (type == AppUpdateType.IMMEDIATE) fake.installCompletes()
    }

    private fun installedVersionCode(): Int =
        context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt()
}
