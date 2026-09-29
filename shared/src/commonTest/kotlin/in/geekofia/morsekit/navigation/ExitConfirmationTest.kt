package `in`.geekofia.morsekit.navigation

import `in`.geekofia.morsekit.navigation.ExitConfirmation.Decision
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class ExitConfirmationTest {
    private val clock = TestTimeSource()
    private val confirmation = ExitConfirmation(window = 2.seconds, clock = clock)

    @Test
    fun firstBackShowsTheHint() {
        assertEquals(Decision.ShowHint, confirmation.onBack())
    }

    @Test
    fun secondBackWithinTheWindowExits() {
        confirmation.onBack()
        clock += 1_500.milliseconds
        assertEquals(Decision.Exit, confirmation.onBack())
    }

    @Test
    fun secondBackExactlyAtTheWindowStillExits() {
        confirmation.onBack()
        clock += 2.seconds
        assertEquals(Decision.Exit, confirmation.onBack())
    }

    @Test
    fun secondBackAfterTheWindowStartsOver() {
        confirmation.onBack()
        clock += 2_001.milliseconds
        assertEquals(Decision.ShowHint, confirmation.onBack())
        clock += 500.milliseconds
        assertEquals(Decision.Exit, confirmation.onBack())
    }

    @Test
    fun afterExitingTheNextBackAsksAgain() {
        confirmation.onBack()
        confirmation.onBack()
        assertEquals(Decision.ShowHint, confirmation.onBack())
    }
}
