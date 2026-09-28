package `in`.geekofia.morsekit.feature.playback

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

/**
 * Runs at most one transmission at a time in [scope]. A new one waits for the previous one to
 * finish (including its `finally` cleanup, e.g. switching the torch off) before starting, so two
 * can never overlap. Shared by the flashlight and vibration ViewModels.
 */
class TransmissionRunner(private val scope: CoroutineScope) {
    private var job: Job? = null

    /** True from [start] until the transmission ends or [stop] is called. */
    var isRunning by mutableStateOf(false)
        private set

    fun start(transmission: suspend () -> Unit) {
        isRunning = true
        val previous = job
        // LAZY: on Dispatchers.Main.immediate an eagerly started body could finish before `job`
        // is assigned below, and the `finally` check would miss it.
        val newJob = scope.launch(start = CoroutineStart.LAZY) {
            val self = coroutineContext[Job]
            previous?.cancelAndJoin()
            try {
                transmission()
            } finally {
                if (job === self) {
                    job = null
                    isRunning = false
                }
            }
        }
        job = newJob
        newJob.start()
    }

    /** Safe to call at any time; the running transmission cleans up as it's cancelled. */
    fun stop() {
        job?.cancel()
        job = null
        isRunning = false
    }
}
