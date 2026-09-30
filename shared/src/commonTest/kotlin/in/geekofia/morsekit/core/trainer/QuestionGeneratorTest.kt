package `in`.geekofia.morsekit.core.trainer

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class QuestionGeneratorTest {
    private val generator = QuestionGenerator(Random(42))

    @Test
    fun choicesAreDistinctUnlockedCharactersIncludingTheTarget() {
        val progress = TrainerProgress(level = 10)
        repeat(500) {
            val q = generator.next(progress)
            assertTrue(q.target in progress.unlocked)
            assertTrue(q.target in q.choices)
            assertEquals(QuestionGenerator.CHOICES, q.choices.size)
            assertEquals(q.choices.size, q.choices.toSet().size)
            assertTrue(q.choices.all { it in progress.unlocked })
        }
    }

    @Test
    fun withTwoCharactersThereAreTwoChoices() {
        val q = generator.next(TrainerProgress())
        assertEquals(setOf('K', 'M'), q.choices.toSet())
    }

    @Test
    fun theSameTargetIsNotAskedTwiceInARowFromThreeCharacters() {
        val progress = TrainerProgress(level = 3)
        var previous: Char? = null
        repeat(500) {
            val q = generator.next(progress, previous)
            assertNotEquals(previous, q.target)
            previous = q.target
        }
    }

    @Test
    fun theNewestCharacterComesUpMoreThanItsFairShare() {
        val progress = TrainerProgress(level = 10)
        val draws = List(4_000) { generator.next(progress).target }
        val newestShare = draws.count { it == progress.newest } / draws.size.toDouble()
        assertTrue(newestShare > 2.0 / progress.level, "newest share was $newestShare")
    }

    @Test
    fun missedCharactersComeBackMoreOften() {
        val progress = TrainerProgress(level = 6, stats = mapOf('K' to CharStats(correct = 0, wrong = 10)))
        val draws = List(4_000) { generator.next(progress).target }
        val missed = draws.count { it == 'K' }
        val steady = draws.count { it == 'M' }
        assertTrue(missed > steady * 2, "K drawn $missed times, M $steady")
    }

    @Test
    fun theSameSeedGivesTheSameQuestions() {
        val progress = TrainerProgress(level = 8)
        val a = QuestionGenerator(Random(7)).let { g -> List(20) { g.next(progress) } }
        val b = QuestionGenerator(Random(7)).let { g -> List(20) { g.next(progress) } }
        assertEquals(a, b)
    }
}
