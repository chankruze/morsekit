package `in`.geekofia.morsekit.feature.trainer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import `in`.geekofia.morsekit.core.settings.SettingsRepository
import `in`.geekofia.morsekit.core.tap.TapMode
import `in`.geekofia.morsekit.core.tap.TapTiming
import `in`.geekofia.morsekit.core.trainer.KochProgression
import `in`.geekofia.morsekit.core.trainer.PracticeSession
import `in`.geekofia.morsekit.core.trainer.QuestionGenerator
import `in`.geekofia.morsekit.core.trainer.TrainerMode
import `in`.geekofia.morsekit.core.trainer.TrainerProgress
import `in`.geekofia.morsekit.core.trainer.TrainerQuestion
import `in`.geekofia.morsekit.core.trainer.TrainerRepository
import `in`.geekofia.morsekit.core.trainer.keyedCorrectly
import `in`.geekofia.morsekit.feature.tap.Keyer
import `in`.geekofia.morsekit.feature.tap.monotonicMillis

/** The answer to the current question: a picked character (Listen) or keyed Morse (Key). */
data class TrainerAnswer(val correct: Boolean, val chosen: Char? = null, val keyed: String? = null)

data class TrainerUiState(
    val progress: TrainerProgress,
    val question: TrainerQuestion,
    val mode: TrainerMode = TrainerMode.Listen,
    val answer: TrainerAnswer? = null,
    /** The current sitting: endless, or counted with a summary at the end. */
    val session: PracticeSession = PracticeSession(),
    /** A finished (or ended) counted session, to summarise. */
    val summary: PracticeSession? = null,
    /** A character just unlocked, to introduce before carrying on. */
    val justUnlocked: Char? = null,
    val hideMorse: Boolean = false,
    /** The last session length chosen, offered first. */
    val sessionLength: Int = PracticeSession.DEFAULT_LENGTH,
)

/**
 * The Koch trainer. Thin: [KochProgression] and [QuestionGenerator] make the decisions, a [Keyer]
 * (the Tap screen's, with its saved speed) handles Key mode; this class asks, grades, records,
 * saves and keeps the session. Both modes feed the same progression. Timing (moving on after a
 * right answer, a keyed letter ending) is driven by the screen, so there are no coroutines here.
 */
class TrainerViewModel(
    private val repository: TrainerRepository,
    private val settingsRepository: SettingsRepository,
    private val generator: QuestionGenerator = QuestionGenerator(),
    clock: () -> Long = monotonicMillis(),
) : ViewModel() {

    val keyer = Keyer(timing = { TapTiming(settingsRepository.settings.value.tapWordsPerMinute) }, clock = clock)

    var uiState by mutableStateOf(
        repository.load().let { progress ->
            TrainerUiState(
                progress = progress,
                question = generator.next(progress),
                mode = repository.mode,
                hideMorse = repository.hideMorse,
                sessionLength = repository.sessionLength,
            )
        },
    )
        private set

    /** How Key mode is keyed: the Tap screen's saved choice. */
    val tapMode: TapMode get() = settingsRepository.settings.value.tapMode

    // --- Listen ------------------------------------------------------------------------------

    /** Records the first answer to the current question; later taps are ignored. */
    fun answer(choice: Char) {
        if (uiState.answer != null || uiState.mode != TrainerMode.Listen) return
        record(TrainerAnswer(correct = choice == uiState.question.target, chosen = choice))
    }

    // --- Key: every keying call checks whether a letter has been committed -------------------

    fun keyPress() = keying { keyer.press() }

    fun keyRelease() = keying { keyer.release() }

    fun keyAdvance() = keying { keyer.advance() }

    fun keyDot() = keying { keyer.dot() }

    fun keyDash() = keying { keyer.dash() }

    /** Buttons mode's Check (and the key's End letter action): ends the letter now. */
    fun keyCheck() = keying { keyer.endLetter() }

    fun keyBackspace() = keying { keyer.backspace() }

    fun setTapMode(mode: TapMode) {
        settingsRepository.setTapMode(mode)
        keyer.stopTimers()
    }

    private inline fun keying(action: () -> Unit) {
        if (uiState.answer != null || uiState.mode != TrainerMode.Key) return
        action()
        // The first letter the keyer commits is the answer.
        val keyed = keyer.state.morse.substringBefore(' ').takeIf { keyer.state.text.isNotEmpty() } ?: return
        keyer.stopTimers()
        record(TrainerAnswer(correct = keyedCorrectly(uiState.question.target, keyed), keyed = keyed))
    }

    // --- Both --------------------------------------------------------------------------------

    private fun record(answer: TrainerAnswer) {
        val state = uiState
        val result = KochProgression.record(state.progress, state.question.target, answer.correct)
        repository.save(result.progress)
        uiState = state.copy(
            progress = result.progress,
            answer = answer,
            session = state.session.record(state.question.target, answer.correct, result.unlocked),
            justUnlocked = result.unlocked,
        )
    }

    /** The next question, or the summary once a counted session is done. */
    fun next() {
        val state = uiState
        keyer.clear()
        val question = generator.next(state.progress, previous = state.question.target)
        uiState = if (state.session.isFinished) {
            state.copy(question = question, answer = null, summary = state.session, session = PracticeSession())
        } else {
            state.copy(question = question, answer = null)
        }
    }

    /** Ignored during a counted session: its mode was chosen when it started. */
    fun setMode(mode: TrainerMode) {
        if (mode == uiState.mode || uiState.session.isCounted) return
        repository.mode = mode
        keyer.clear()
        uiState = uiState.copy(mode = mode, question = generator.next(uiState.progress, uiState.question.target), answer = null)
    }

    fun startSession(length: Int) {
        repository.sessionLength = length
        keyer.clear()
        val state = uiState
        uiState = state.copy(
            session = PracticeSession(length = length),
            sessionLength = length,
            question = generator.next(state.progress, state.question.target),
            answer = null,
        )
    }

    /** Stops a counted session early; it's summarised if anything was answered. */
    fun endSession() {
        val session = uiState.session
        uiState = uiState.copy(session = PracticeSession(), summary = session.takeIf { it.answered > 0 })
    }

    fun dismissSummary() {
        uiState = uiState.copy(summary = null)
    }

    fun dismissUnlocked() {
        uiState = uiState.copy(justUnlocked = null)
    }

    fun setHideMorse(hide: Boolean) {
        repository.hideMorse = hide
        uiState = uiState.copy(hideMorse = hide)
    }

    /** Back to K and M; the session starts over too. */
    fun resetProgress() {
        repository.reset()
        keyer.clear()
        val progress = repository.load()
        uiState = uiState.copy(
            progress = progress,
            question = generator.next(progress),
            answer = null,
            session = PracticeSession(),
            summary = null,
            justUnlocked = null,
        )
    }
}
