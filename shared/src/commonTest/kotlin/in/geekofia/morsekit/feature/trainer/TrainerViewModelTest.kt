package `in`.geekofia.morsekit.feature.trainer

import `in`.geekofia.morsekit.core.morse.MorseAlphabet
import `in`.geekofia.morsekit.core.settings.SettingsRepository
import `in`.geekofia.morsekit.core.tap.TapMode
import `in`.geekofia.morsekit.core.trainer.CharStats
import `in`.geekofia.morsekit.core.trainer.PracticeSession
import `in`.geekofia.morsekit.core.trainer.QuestionGenerator
import `in`.geekofia.morsekit.core.trainer.TrainerMode
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
    private val settings = SettingsRepository(store)
    private var now = 0L
    private fun viewModel() = TrainerViewModel(repository, settings, QuestionGenerator(Random(1)), clock = { now })

    private fun TrainerViewModel.answerRight() = answer(uiState.question.target)
    private fun TrainerViewModel.answerWrong() = answer(uiState.question.choices.first { it != uiState.question.target })

    private fun codeOf(char: Char) = MorseAlphabet.International.codeFor(char)!!.code

    /** Keys [code] with the Timing key at the default 8 WPM, then pauses past the letter gap. */
    private fun TrainerViewModel.keyWithTiming(code: String) {
        code.forEach { element ->
            keyPress()
            now += if (element == '.') 100 else 400
            keyRelease()
            now += 150
        }
        now += 450
        keyAdvance()
    }

    private fun TrainerViewModel.keyWithButtons(code: String) {
        code.forEach { if (it == '.') keyDot() else keyDash() }
        keyCheck()
    }

    @Test
    fun startsWithKAndMInListenMode() {
        val vm = viewModel()
        assertEquals(listOf('K', 'M'), vm.uiState.progress.unlocked)
        assertEquals(TrainerMode.Listen, vm.uiState.mode)
        assertTrue(vm.uiState.question.target in listOf('K', 'M'))
        assertNull(vm.uiState.answer)
        assertFalse(vm.uiState.session.isCounted)
    }

    @Test
    fun rightAndWrongAnswersAreScoredAndSaved() {
        val vm = viewModel()
        val first = vm.uiState.question.target
        vm.answerRight()
        assertEquals(TrainerAnswer(correct = true, chosen = first), vm.uiState.answer)
        vm.next()
        vm.answerWrong()
        assertFalse(vm.uiState.answer!!.correct)
        assertEquals(2, vm.uiState.session.answered)
        assertEquals(1, vm.uiState.session.correct)
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
    fun eighteenOfTwentyUnlocksAndIntroducesTheNextCharacter() {
        val vm = viewModel()
        repeat(20) { i ->
            if (i < 2) vm.answerWrong() else vm.answerRight()
            if (i < 19) vm.next()
        }
        assertEquals('U', vm.uiState.justUnlocked)
        assertEquals(listOf('U'), vm.uiState.session.unlocked)
        vm.dismissUnlocked()
        assertNull(vm.uiState.justUnlocked)
        assertEquals(3, TrainerRepository(store).load().level)
    }

    @Test
    fun keyModeGradesATimedLetterWhenItsPauseEnds() {
        val vm = viewModel()
        vm.setMode(TrainerMode.Key)
        val target = vm.uiState.question.target
        vm.keyWithTiming(codeOf(target))
        assertEquals(TrainerAnswer(correct = true, keyed = codeOf(target)), vm.uiState.answer)
        assertNull(vm.keyer.millisUntilNextChange(), "no timer left running after grading")
    }

    @Test
    fun keyModeWithButtonsGradesOnCheck() {
        settings.setTapMode(TapMode.Buttons)
        val vm = viewModel()
        vm.setMode(TrainerMode.Key)
        assertEquals(TapMode.Buttons, vm.tapMode)
        vm.keyWithButtons("........")
        val answer = assertNotNull(vm.uiState.answer)
        assertFalse(answer.correct)
        assertEquals("........", answer.keyed)
    }

    @Test
    fun keyingAndListeningShareTheProgression() {
        val vm = viewModel()
        vm.answerRight()
        vm.next()
        vm.setMode(TrainerMode.Key)
        vm.keyWithButtons(codeOf(vm.uiState.question.target))
        assertEquals(listOf(true, true), repository.load().recent)
        assertEquals(2, vm.uiState.session.answered)
    }

    @Test
    fun keyingIsIgnoredInListenModeAndPickingInKeyMode() {
        val vm = viewModel()
        vm.keyWithButtons("-.-")
        assertNull(vm.uiState.answer)
        vm.setMode(TrainerMode.Key)
        vm.answer(vm.uiState.question.target)
        assertNull(vm.uiState.answer)
    }

    @Test
    fun switchingModeIsRememberedAndAsksAFreshQuestion() {
        val vm = viewModel()
        vm.answerWrong()
        vm.setMode(TrainerMode.Key)
        assertNull(vm.uiState.answer)
        assertEquals(TrainerMode.Key, repository.mode)
        assertEquals(TrainerMode.Key, viewModel().uiState.mode)
    }

    @Test
    fun aCountedSessionEndsWithASummary() {
        val vm = viewModel()
        vm.startSession(10)
        assertEquals(10, repository.sessionLength)
        repeat(10) { i ->
            if (i == 0) vm.answerWrong() else vm.answerRight()
            vm.next()
        }
        val summary = assertNotNull(vm.uiState.summary)
        assertEquals(10, summary.answered)
        assertEquals(90, summary.accuracyPercent)
        assertEquals(1, summary.misses.values.sum())
        assertFalse(vm.uiState.session.isCounted, "back to endless practice")
        vm.dismissSummary()
        assertNull(vm.uiState.summary)
    }

    @Test
    fun theModeIsFixedDuringASession() {
        val vm = viewModel()
        vm.setMode(TrainerMode.Key)
        vm.startSession(10)
        vm.setMode(TrainerMode.Listen)
        assertEquals(TrainerMode.Key, vm.uiState.mode)
        vm.endSession()
        vm.setMode(TrainerMode.Listen)
        assertEquals(TrainerMode.Listen, vm.uiState.mode)
    }

    @Test
    fun endingASessionEarlySummarisesWhatWasAnswered() {
        val vm = viewModel()
        vm.startSession(20)
        vm.endSession()
        assertNull(vm.uiState.summary, "nothing answered, nothing to summarise")
        vm.startSession(20)
        vm.answerRight()
        vm.endSession()
        assertEquals(1, vm.uiState.summary?.answered)
        assertEquals(PracticeSession(), vm.uiState.session)
    }

    @Test
    fun progressAndOptionsSurviveARestart() {
        repository.save(TrainerProgress(level = 7))
        viewModel().run {
            setHideMorse(true)
            startSession(50)
        }
        val restarted = viewModel()
        assertEquals(7, restarted.uiState.progress.level)
        assertTrue(restarted.uiState.hideMorse)
        assertEquals(50, restarted.uiState.sessionLength)
        assertFalse(restarted.uiState.session.isCounted, "a session doesn't survive a restart")
    }

    @Test
    fun resetGoesBackToTheStartButKeepsTheOptions() {
        repository.save(TrainerProgress(level = 9))
        val vm = viewModel()
        vm.setHideMorse(true)
        vm.answerRight()
        vm.resetProgress()
        assertEquals(TrainerProgress(), vm.uiState.progress)
        assertEquals(PracticeSession(), vm.uiState.session)
        assertTrue(vm.uiState.hideMorse)
    }
}
