package `in`.geekofia.morsekit.feature.playback

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

/** Real coroutines on a single-threaded runBlocking loop; ordering is deterministic. */
class TransmissionRunnerTest {

    private suspend fun awaitIdle(runner: TransmissionRunner) {
        repeat(1_000) {
            if (!runner.isRunning) return
            yield()
        }
        fail("runner never became idle")
    }

    @Test
    fun isRunningUntilTheTransmissionEnds() = runBlocking {
        val runner = TransmissionRunner(this)
        val finish = CompletableDeferred<Unit>()
        runner.start { finish.await() }
        assertTrue(runner.isRunning)
        yield()
        assertTrue(runner.isRunning)
        finish.complete(Unit)
        awaitIdle(runner)
        assertFalse(runner.isRunning)
    }

    @Test
    fun stopCancelsAndRunsCleanup() = runBlocking {
        val runner = TransmissionRunner(this)
        val log = mutableListOf<String>()
        runner.start {
            try {
                awaitCancellation()
            } finally {
                log += "cleanup"
            }
        }
        yield()
        runner.stop()
        assertFalse(runner.isRunning)
        yield()
        assertEquals(listOf("cleanup"), log)
    }

    @Test
    fun newTransmissionWaitsForThePreviousCleanup() = runBlocking {
        val runner = TransmissionRunner(this)
        val log = mutableListOf<String>()
        runner.start {
            try {
                awaitCancellation()
            } finally {
                withContext(NonCancellable) {
                    log += "first cleanup starts"
                    yield() // a slow cleanup, e.g. switching hardware off
                    log += "first cleanup ends"
                }
            }
        }
        yield()
        runner.start { log += "second starts" }
        awaitIdle(runner)
        assertEquals(listOf("first cleanup starts", "first cleanup ends", "second starts"), log)
    }

    @Test
    fun staleFinishDoesNotClearANewerRun() = runBlocking {
        val runner = TransmissionRunner(this)
        val finishSecond = CompletableDeferred<Unit>()
        runner.start { awaitCancellation() }
        yield()
        runner.start { finishSecond.await() }
        repeat(10) { yield() } // the first run is cancelled and finishes
        assertTrue(runner.isRunning, "the first run's finish must not clear the second")
        finishSecond.complete(Unit)
        awaitIdle(runner)
    }

    @Test
    fun stopWhenIdleIsHarmless() = runBlocking {
        val runner = TransmissionRunner(this)
        runner.stop()
        assertFalse(runner.isRunning)
    }
}
