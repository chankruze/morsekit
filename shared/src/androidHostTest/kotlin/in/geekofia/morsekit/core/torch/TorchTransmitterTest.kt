package `in`.geekofia.morsekit.core.torch

import `in`.geekofia.morsekit.core.morse.MorseCodec
import `in`.geekofia.morsekit.core.timing.MorseTiming
import `in`.geekofia.morsekit.platform.TorchController
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

/**
 * The runner is a suspend function, so it's tested on the JVM host where `runBlocking` exists
 * (commonTest has no coroutine test runner without adding kotlinx-coroutines-test).
 * Time is fake: it only advances when the runner sleeps, so tests are instant and exact.
 */
class TorchTransmitterTest {
    private val codec = MorseCodec()
    private val clock = TestTimeSource()

    /** Records (time, state) for every switch. */
    private class FakeTorch(private val clock: TestTimeSource, var works: Boolean = true) : TorchController {
        private val start = clock.markNow()
        val switches = mutableListOf<Pair<Duration, Boolean>>()
        override val isAvailable = true
        override fun setTorch(on: Boolean): Boolean {
            switches += start.elapsedNow() to on
            return works || !on
        }
    }

    private fun plan(text: String, wpm: Int = 20) = TorchPlan.of(codec.encode(text).message, MorseTiming(wpm))

    private val exactSleep: suspend (Duration) -> Unit = { clock += it }

    @Test
    fun switchesAtExactSignalBoundariesAndEndsOff() = runBlocking {
        val torch = FakeTorch(clock)
        transmitWithTorch(plan("A"), torch, clock, exactSleep)
        assertEquals(
            listOf(0.ms to true, 60.ms to false, 120.ms to true, 300.ms to false, 300.ms to false),
            torch.switches,
        )
    }

    @Test
    fun lateWakeUpsDoNotAccumulate() = runBlocking {
        val torch = FakeTorch(clock)
        // Every sleep overruns by 7 ms, like a busy main thread.
        transmitWithTorch(plan("EEE"), torch, clock) { clock += it + 7.milliseconds }
        // E gap E gap E: on at 0, 240, 480 (units 0, 4, 8). Each switch is at most 7 ms late,
        // not 7 ms more late each time.
        val onTimes = torch.switches.filter { it.second }.map { it.first }
        assertEquals(listOf(0.ms, 247.ms, 487.ms), onTimes)
    }

    @Test
    fun overrunPastAWholeSignalSkipsToTheCurrentState() = runBlocking {
        val torch = FakeTorch(clock)
        // One huge stall after the first switch jumps past the whole letter.
        var first = true
        transmitWithTorch(plan("A"), torch, clock) {
            clock += if (first) 1_000.milliseconds else it
            first = false
        }
        assertEquals(listOf(0.ms to true, 1_000.ms to false, 1_000.ms to false), torch.switches)
    }

    @Test
    fun cancellationTurnsTheTorchOff() = runBlocking {
        val torch = FakeTorch(clock)
        val sleeping = CompletableDeferred<Unit>()
        val job = launch {
            transmitWithTorch(plan("T"), torch, clock) {
                sleeping.complete(Unit)
                kotlinx.coroutines.awaitCancellation()
            }
        }
        sleeping.await()
        job.cancelAndJoin()
        assertEquals(listOf(0.ms to true, 0.ms to false), torch.switches)
    }

    @Test
    fun failureToSwitchOnThrowsAndLeavesTheTorchOff() = runBlocking {
        val torch = FakeTorch(clock, works = false)
        val result = async { runCatching { transmitWithTorch(plan("E"), torch, clock, exactSleep) } }
        assertFailsWith<TorchFailedException> { result.await().getOrThrow() }
        assertEquals(false, torch.switches.last().second)
    }

    @Test
    fun emptyPlanJustEnsuresTheTorchIsOff() = runBlocking {
        val torch = FakeTorch(clock)
        transmitWithTorch(plan(""), torch, clock, exactSleep)
        assertEquals(listOf(0.ms to false, 0.ms to false), torch.switches)
    }

    private val Int.ms get() = this.milliseconds
}
