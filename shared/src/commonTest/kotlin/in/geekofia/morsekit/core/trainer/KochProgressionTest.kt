package `in`.geekofia.morsekit.core.trainer

import `in`.geekofia.morsekit.core.morse.MorseAlphabet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KochProgressionTest {
    private fun answer(progress: TrainerProgress, vararg correct: Boolean): KochProgression.Result {
        var result = KochProgression.Result(progress, unlocked = null)
        correct.forEach { result = KochProgression.record(result.progress, result.progress.newest, it) }
        return result
    }

    @Test
    fun kochOrderIsFortyOneKnownCharactersStartingWithKAndM() {
        assertEquals(41, KochOrder.characters.size)
        assertEquals(KochOrder.characters.size, KochOrder.characters.toSet().size)
        assertTrue(KochOrder.characters.all { it in MorseAlphabet.International })
        assertTrue(KochOrder.characters.containsAll(('A'..'Z').toList() + ('0'..'9').toList()))
        assertEquals(listOf('K', 'M'), TrainerProgress().unlocked)
    }

    @Test
    fun answersAreCountedPerCharacter() {
        var p = KochProgression.record(TrainerProgress(), 'K', correct = true).progress
        p = KochProgression.record(p, 'K', correct = false).progress
        p = KochProgression.record(p, 'M', correct = true).progress
        assertEquals(CharStats(correct = 1, wrong = 1), p.stats['K'])
        assertEquals(CharStats(correct = 1), p.stats['M'])
        assertEquals(listOf(true, false, true), p.recent)
    }

    @Test
    fun eighteenOfTwentyUnlocksTheNextCharacterAndRestartsTheWindow() {
        val result = answer(TrainerProgress(), *BooleanArray(18) { true }, false, false)
        assertEquals('U', result.unlocked)
        assertEquals(3, result.progress.level)
        assertEquals(emptyList(), result.progress.recent)
    }

    @Test
    fun seventeenOfTwentyDoesNot() {
        val result = answer(TrainerProgress(), *BooleanArray(17) { true }, false, false, false)
        assertNull(result.unlocked)
        assertEquals(2, result.progress.level)
        assertEquals(20, result.progress.recent.size)
    }

    @Test
    fun onlyTheLastTwentyAnswersCount() {
        // Ten early misses fall out of the window once twenty good answers follow.
        val result = answer(TrainerProgress(), *BooleanArray(10) { false }, *BooleanArray(19) { true })
        assertEquals(3, result.progress.level)
    }

    @Test
    fun nothingUnlocksAfterTheLastCharacter() {
        val done = TrainerProgress(level = KochOrder.characters.size)
        assertTrue(done.isComplete)
        val result = answer(done, *BooleanArray(20) { true })
        assertNull(result.unlocked)
        assertEquals(KochOrder.characters.size, result.progress.level)
    }

    @Test
    fun progressRejectsImpossibleValues() {
        assertFailsWith<IllegalArgumentException> { TrainerProgress(level = 1) }
        assertFailsWith<IllegalArgumentException> { TrainerProgress(level = 42) }
        assertFailsWith<IllegalArgumentException> { TrainerProgress(recent = List(21) { true }) }
        assertFailsWith<IllegalArgumentException> { CharStats(correct = -1) }
    }
}
