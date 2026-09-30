package `in`.geekofia.morsekit.core.timing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StepWpmTest {

    @Test
    fun stepsCoverTheWholeSupportedRange() {
        assertEquals(MorseTiming.MIN_WPM, WPM_STEPS.first())
        assertEquals(MorseTiming.MAX_WPM, WPM_STEPS.last())
        assertEquals(WPM_STEPS.sorted().distinct(), WPM_STEPS)
    }

    @Test
    fun movesToTheNeighbouringStep() {
        assertEquals(25, stepWpm(20, faster = true))
        assertEquals(18, stepWpm(20, faster = false))
        assertEquals(13, stepWpm(12, faster = true))
    }

    @Test
    fun valueBetweenStepsSnapsInTheChosenDirection() {
        // 17 can be set with the Settings slider.
        assertEquals(18, stepWpm(17, faster = true))
        assertEquals(15, stepWpm(17, faster = false))
    }

    @Test
    fun endsStayPut() {
        assertEquals(60, stepWpm(60, faster = true))
        assertEquals(5, stepWpm(5, faster = false))
    }

    @Test
    fun outOfRangeValuesAreClampedFirst() {
        assertEquals(60, stepWpm(99, faster = true))
        assertEquals(5, stepWpm(0, faster = false))
    }

    @Test
    fun twentyToSixtyTakesAFewTaps() {
        var wpm = 20
        var taps = 0
        while (wpm < 60) {
            wpm = stepWpm(wpm, faster = true)
            taps++
        }
        assertTrue(taps <= 7, "took $taps taps")
    }
}
