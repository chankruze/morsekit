package `in`.geekofia.morsekit.core.update

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import `in`.geekofia.morsekit.platform.AppUpdateService

/**
 * App-scoped orchestration of in-app updates. Synchronous and main-thread only (like the
 * [AppUpdateService] contract), so it's tested with a fake service.
 */
class UpdateController(
    private val service: AppUpdateService,
    private val prompter: UpdatePrompter,
) {
    enum class ManualResult { UpToDate, Offered }

    /** A flexible update is downloaded and waiting for the user to restart. */
    var readyToInstall by mutableStateOf(false)
        private set

    /** Outcome of the last "Check for updates", until [dismissManualResult]. */
    var manualResult by mutableStateOf<ManualResult?>(null)
        private set

    val isSupported: Boolean get() = service.isSupported

    init {
        service.setOnReadyToInstall { readyToInstall = true }
    }

    /**
     * Call when the app comes to the foreground. Always resumes an interrupted immediate update;
     * offers a new update only if [UpdatePrompter] says a check is due.
     */
    fun onAppResumed() {
        if (!service.isSupported) return
        service.checkForUpdate(manual = false) { update ->
            when {
                update == null -> Unit
                update.immediateInProgress -> service.startUpdate(UpdateMode.Immediate) {}
                prompter.takeCheckOpportunity() -> offer(update, manual = false)
            }
        }
    }

    /** "Check for updates": bypasses the daily limit and the snooze. */
    fun checkNow() {
        if (!service.isSupported) return
        service.checkForUpdate(manual = true) { update ->
            val offered = update != null && offer(update, manual = true)
            manualResult = if (offered) ManualResult.Offered else ManualResult.UpToDate
        }
    }

    fun installNow() {
        readyToInstall = false
        service.completeUpdate()
    }

    fun postponeInstall() {
        readyToInstall = false
    }

    fun dismissManualResult() {
        manualResult = null
    }

    /** Returns true if an update flow was started. */
    private fun offer(update: AvailableUpdate, manual: Boolean): Boolean {
        val mode = prompter.modeToOffer(update, manual) ?: return false
        service.startUpdate(mode) { accepted ->
            if (!accepted && mode == UpdateMode.Flexible) prompter.onDeclined(update.versionCode)
        }
        return true
    }
}
