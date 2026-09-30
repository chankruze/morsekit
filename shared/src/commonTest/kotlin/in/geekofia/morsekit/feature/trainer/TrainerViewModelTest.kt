package `in`.geekofia.morsekit.feature.trainer

import `in`.geekofia.morsekit.core.trainer.CharStats
import `in`.geekofia.morsekit.core.trainer.QuestionGenerator
import `in`.geekofia.morsekit.core.trainer.TrainerProgress
import `in`.geekofia.morsekit.core.trainer.TrainerRepository
import `in`.geekofia.morsekit.platform.InMemoryKeyValueStore
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TrainerViewModelTest {
    private val store = InMemoryKeyValueStore()
    private val repository = TrainerRepository(store)
    private fun viewModel() = TrainerViewModel(repository, QuestionGenerator(Random(1)))

    private fun TrainerViewModel.answerRight() = answer(uiState.question.target)
    private fun TrainerViewModel.answerWrong() = answer(uiState.question.choices.first { it != uiState.question.target })

    @Test
    fun startsWithKAndM() {
        val vm = viewModel()
        assertEquals(listOf('K', 'M'), vm.uiState.progress.unlocked)
        assertTrue(vm.uiState.question.target in listOf('K', 'M'))
        assertNull(vm.uiState.answer)
    }

    @Test
    fun rightAndWrongAnswersAreScoredAndSaved() {
        val vm = viewModel()
        val first = vm.uiState.question.target
        vm.answerRight()
        assertEquals(TrainerAnswer(first, correct = true), vm.uiState.answer)
        vm.next()
        vm.answerWrong()
        assertFalse(vm.uiState.answer!!.correct)
        assertEquals(SessionScore(answered = 2, correct = 1, streak = 0), vm.uiState.session)
        val saved = repository.load()
        assertEquals(2, saved.recent.size)
        assertEquals(2, saved.stats.values.sumOf(CharStats::attempts))
    }

    @Test
    fun onlyTheFirstAnswerCounts() {
        val vm = viewModel()
        vm.answerWrong()
        vm.answerRight()
        assertFalse(vm.uiState.answer!!.correct)
        assertEquals(1, vm.uiState.session.answered)
    }

    @Test
    fun streakCountsConsecutiveRightAnswers() {
        val vm = viewModel()
        repeat(3) { vm.answerRight(); vm.next() }
        assertEquals(3, vm.uiState.session.streak)
        vm.answerWrong()
        assertEquals(0, vm.uiState.session.streak)
    }

    @Test
    fun eighteenOfTwentyUnlocksAndIntroducesTheNextCharacter() {
        val vm = viewModel()
        repeat(20) { i ->
            if (i < 2) vm.answerWrong() else vm.answerRight()
            if (i < 19) vm.next()
        }
        assertEquals('U', vm.uiState.justUnlocked)
        assertEquals(3, vm.uiState.progress.level)
        vm.dismissUnlocked()
        assertNull(vm.uiState.justUnlocked)
        assertEquals(3, TrainerRepository(store).load().level)
    }

    @Test
    fun nextAsksANewQuestion() {
        repository.save(TrainerProgress(level = 5))
        val vm = viewModel()
        val first = vm.uiState.question.target
        vm.answerRight()
        vm.next()
        assertNull(vm.uiState.answer)
        assertTrue(vm.uiState.question.target != first)
    }

    @Test
    fun progressAndHideMorseSurviveARestart() {
        repository.save(TrainerProgress(level = 7))
        viewModel().setHideMorse(true)
        val restarted = viewModel()
        assertEquals(7, restarted.uiState.progress.level)
        assertTrue(restarted.uiState.hideMorse)
    }

    @Test
    fun resetGoesBackToTheStartButKeepsTheOption() {
        repository.save(TrainerProgress(level = 9))
        val vm = viewModel()
        vm.setHideMorse(true)
        vm.answerRight()
        vm.resetProgress()
        assertEquals(TrainerProgress(), vm.uiState.progress)
        assertEquals(SessionScore(), vm.uiState.session)
        assertTrue(vm.uiState.hideMorse)
        assertNotNull(vm.uiState.question)
    }
}
