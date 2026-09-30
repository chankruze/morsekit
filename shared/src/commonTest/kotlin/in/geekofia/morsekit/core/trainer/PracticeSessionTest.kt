package `in`.geekofia.morsekit.core.trainer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PracticeSessionTest {
    @Test
    fun endlessPracticeNeverFinishes() {
        var s = PracticeSession()
        repeat(100) { s = s.record('K', correct = true) }
        assertFalse(s.isCounted)
        assertFalse(s.isFinished)
        assertEquals(100, s.streak)
    }

    @Test
    fun aCountedSessionFinishesAfterItsLength() {
        var s = PracticeSession(length = 10)
        repeat(9) { s = s.record('K', correct = true) }
        assertFalse(s.isFinished)
        s = s.record('M', correct = false)
        assertTrue(s.isFinished)
        assertEquals(90, s.accuracyPercent)
        assertEquals(0, s.streak)
    }

    @Test
    fun missesAreCountedAndSortedMostFirst() {
        val s = PracticeSession(length = 10)
            .record('M', correct = false)
            .record('K', correct = false)
            .record('K', correct = false)
            .record('U', correct = false)
            .record('R', correct = true)
        assertEquals(listOf('K' to 2, 'M' to 1, 'U' to 1), s.missesByCount)
    }

    @Test
    fun unlocksAreCollectedInOrder() {
        val s = PracticeSession().record('K', true, unlockedChar = 'U').record('M', true).record('U', true, unlockedChar = 'R')
        assertEquals(listOf('U', 'R'), s.unlocked)
    }

    @Test
    fun accuracyIsRoundedAndAbsentBeforeAnswers() {
        assertNull(PracticeSession().accuracyPercent)
        val twoOfThree = PracticeSession().record('K', true).record('K', true).record('K', false)
        assertEquals(67, twoOfThree.accuracyPercent)
    }

    @Test
    fun lengthsAreSensible() {
        assertTrue(PracticeSession.DEFAULT_LENGTH in PracticeSession.LENGTHS)
        assertFailsWith<IllegalArgumentException> { PracticeSession(length = 0) }
    }

    @Test
    fun keyedAnswersAreCheckedAgainstTheAlphabet() {
        assertTrue(keyedCorrectly('K', "-.-"))
        assertFalse(keyedCorrectly('K', "-.."))
        assertFalse(keyedCorrectly('K', ""))
        assertTrue(keyedCorrectly('=', "-...-"))
    }
}
