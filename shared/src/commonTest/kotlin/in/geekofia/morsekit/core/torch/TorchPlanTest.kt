package `in`.geekofia.morsekit.core.torch

import `in`.geekofia.morsekit.core.morse.MorseCodec
import `in`.geekofia.morsekit.core.timing.MorseTiming
import `in`.geekofia.morsekit.core.timing.toSignals
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class TorchPlanTest {
    private val codec = MorseCodec()

    private fun plan(text: String, wpm: Int = 20) = TorchPlan.of(codec.encode(text).message, MorseTiming(wpm))

    private fun on(next: Int) = TorchStep(isOn = true, nextChangeIn = next.milliseconds)
    private fun off(next: Int) = TorchStep(isOn = false, nextChangeIn = next.milliseconds)
    private val done = TorchStep(isOn = false, nextChangeIn = null)

    @Test
    fun dotIsOnForOneUnit() {
        // 20 WPM → 60 ms unit.
        val e = plan("E")
        assertEquals(on(60), e.stepAt(Duration.ZERO))
        assertEquals(on(1), e.stepAt(59.milliseconds))
        assertEquals(done, e.stepAt(60.milliseconds))
    }

    @Test
    fun letterFollowsDotGapDash() {
        // A = on 60, off 60, on 180
        val a = plan("A")
        assertEquals(on(60), a.stepAt(0.milliseconds))
        assertEquals(off(60), a.stepAt(60.milliseconds))
        assertEquals(off(30), a.stepAt(90.milliseconds))
        assertEquals(on(180), a.stepAt(120.milliseconds))
        assertEquals(done, a.stepAt(300.milliseconds))
    }

    @Test
    fun letterAndWordGaps() {
        assertEquals(off(180), plan("ET").stepAt(60.milliseconds)) // 3-unit letter gap
        assertEquals(off(420), plan("E E").stepAt(60.milliseconds)) // 7-unit word gap
    }

    @Test
    fun boundariesBelongToTheNextSignal() {
        val a = plan("A")
        assertFalse(a.stepAt(60.milliseconds).isOn)
        assertTrue(a.stepAt(120.milliseconds).isOn)
    }

    @Test
    fun durationMatchesMorseTiming() {
        val message = codec.encode("PARIS").message
        val timing = MorseTiming(13)
        assertEquals(timing.totalDuration(message.toSignals()), TorchPlan.of(message, timing).duration)
    }

    @Test
    fun speedScalesTheTiming() {
        assertEquals(plan("SOS", wpm = 10).duration, plan("SOS", wpm = 20).duration * 2)
        assertEquals(on(120), plan("E", wpm = 10).stepAt(Duration.ZERO))
    }

    @Test
    fun endsOffAndStaysOff() {
        val sos = plan("SOS")
        assertEquals(done, sos.stepAt(sos.duration))
        assertEquals(done, sos.stepAt(sos.duration + 10_000.milliseconds))
    }

    @Test
    fun emptyMessageIsDoneImmediately() {
        assertEquals(done, plan("").stepAt(Duration.ZERO))
        assertEquals(Duration.ZERO, plan("").duration)
    }

    @Test
    fun walkingTheStepsReproducesTheSignals() {
        val message = codec.encode("HELLO WORLD").message
        val timing = MorseTiming(20)
        val plan = TorchPlan.of(message, timing)
        var elapsed = Duration.ZERO
        val walked = mutableListOf<Pair<Boolean, Duration>>()
        while (true) {
            val step = plan.stepAt(elapsed)
            val next = step.nextChangeIn ?: break
            walked += step.isOn to next
            elapsed += next
        }
        assertEquals(message.toSignals().map { it.isOn to timing.durationOf(it) }, walked)
    }

    @Test
    fun negativeElapsedIsRejected() {
        assertFailsWith<IllegalArgumentException> { plan("E").stepAt((-1).milliseconds) }
    }

    @Test
    fun longMessagesAreRejected() {
        val long = codec.encode("PARIS ".repeat(30)).message // 6 minutes at 5 WPM
        assertTrue(TorchPlan.isTooLong(long, MorseTiming(5)))
        assertFalse(TorchPlan.isTooLong(long, MorseTiming(20)))
    }
}
