package `in`.geekofia.morsekit.core.vibration

import `in`.geekofia.morsekit.core.morse.MorseCodec
import `in`.geekofia.morsekit.core.timing.MorseTiming
import `in`.geekofia.morsekit.platform.VibrationController
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/** Suspend code, so tested on the JVM host with runBlocking and an injected sleep (no real waiting). */
class VibrationTransmitterTest {
    private val codec = MorseCodec()

    private class FakeVibration(private val works: Boolean = true) : VibrationController {
        val calls = mutableListOf<String>()
        override val isAvailable = true
        override fun vibrate(pattern: VibrationPattern): Boolean {
            calls += "vibrate ${pattern.durationMillis}ms"
            return works
        }
        override fun cancel() {
            calls += "cancel"
        }
    }

    private fun pattern(text: String) = VibrationPattern.of(codec.encode(text).message, MorseTiming(20))

    @Test
    fun playsThePatternAndWaitsForItWithoutClippingTheEnd() = runBlocking {
        val vibration = FakeVibration()
        val slept = mutableListOf<Duration>()
        transmitWithVibration(pattern("A"), vibration) { slept += it }
        // No cancel after a normal finish: the OS starts the pattern slightly late, so cancelling
        // exactly on time would cut off the last element.
        assertEquals(listOf("vibrate 300ms"), vibration.calls)
        assertEquals(listOf(300.milliseconds), slept)
    }

    @Test
    fun cancellationStopsTheVibration() = runBlocking {
        val vibration = FakeVibration()
        val waiting = CompletableDeferred<Unit>()
        val job = launch {
            transmitWithVibration(pattern("SOS"), vibration) {
                waiting.complete(Unit)
                awaitCancellation()
            }
        }
        waiting.await()
        job.cancelAndJoin()
        assertEquals("cancel", vibration.calls.last())
    }

    @Test
    fun failureToStartThrowsAndStillCancels() = runBlocking {
        val vibration = FakeVibration(works = false)
        assertFailsWith<VibrationFailedException> { transmitWithVibration(pattern("E"), vibration) {} }
        assertEquals(listOf("vibrate 60ms", "cancel"), vibration.calls)
    }

    @Test
    fun emptyPatternDoesNothing() = runBlocking {
        val vibration = FakeVibration()
        transmitWithVibration(pattern(""), vibration) {}
        assertEquals(emptyList(), vibration.calls)
    }
}
