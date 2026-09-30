package `in`.geekofia.morsekit.feature.trainer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import `in`.geekofia.morsekit.core.trainer.KochProgression
import `in`.geekofia.morsekit.core.trainer.QuestionGenerator
import `in`.geekofia.morsekit.core.trainer.TrainerProgress
import `in`.geekofia.morsekit.core.trainer.TrainerQuestion
import `in`.geekofia.morsekit.core.trainer.TrainerRepository

/** The answer to the current question, once given. */
data class TrainerAnswer(val chosen: Char, val correct: Boolean)

/** This visit's score; not saved (the per-character stats in [TrainerProgress] are). */
data class SessionScore(val answered: Int = 0, val correct: Int = 0, val streak: Int = 0) {
    fun record(correct: Boolean) = SessionScore(
        answered = answered + 1,
        correct = this.correct + if (correct) 1 else 0,
        streak = if (correct) streak + 1 else 0,
    )
}

data class TrainerUiState(
    val progress: TrainerProgress,
    val question: TrainerQuestion,
    val answer: TrainerAnswer? = null,
    val session: SessionScore = SessionScore(),
    /** A character just unlocked, to introduce before carrying on. */
    val justUnlocked: Char? = null,
    val hideMorse: Boolean = false,
)

/**
 * Character mode of the Koch trainer. Thin: [KochProgression] and [QuestionGenerator] make the
 * decisions; this class asks, records, saves and keeps the session score. Timing (moving on after
 * a right answer) is the screen's job, so there are no coroutines here.
 */
class TrainerViewModel(
    private val repository: TrainerRepository,
    private val generator: QuestionGenerator = QuestionGenerator(),
) : ViewModel() {

    var uiState by mutableStateOf(
        repository.load().let { progress ->
            TrainerUiState(progress = progress, question = generator.next(progress), hideMorse = repository.hideMorse)
        },
    )
        private set

    /** Records the first answer to the current question; later taps are ignored. */
    fun answer(choice: Char) {
        val state = uiState
        if (state.answer != null) return
        val correct = choice == state.question.target
        val result = KochProgression.record(state.progress, state.question.target, correct)
        repository.save(result.progress)
        uiState = state.copy(
            progress = result.progress,
            answer = TrainerAnswer(choice, correct),
            session = state.session.record(correct),
            justUnlocked = result.unlocked,
        )
    }

    fun next() {
        val state = uiState
        uiState = state.copy(question = generator.next(state.progress, previous = state.question.target), answer = null)
    }

    fun dismissUnlocked() {
        uiState = uiState.copy(justUnlocked = null)
    }

    fun setHideMorse(hide: Boolean) {
        repository.hideMorse = hide
        uiState = uiState.copy(hideMorse = hide)
    }

    /** Back to K and M; the session score starts over too. */
    fun resetProgress() {
        repository.reset()
        val progress = repository.load()
        uiState = TrainerUiState(progress = progress, question = generator.next(progress), hideMorse = uiState.hideMorse)
    }
}
