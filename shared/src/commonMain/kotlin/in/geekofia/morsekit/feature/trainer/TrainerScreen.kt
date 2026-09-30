package `in`.geekofia.morsekit.feature.trainer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.geekofia.morsekit.core.morse.MorseAlphabet
import `in`.geekofia.morsekit.core.morse.MorseCodec
import `in`.geekofia.morsekit.core.review.ReviewPrompter
import `in`.geekofia.morsekit.core.settings.SettingsRepository
import `in`.geekofia.morsekit.core.tap.TapMode
import `in`.geekofia.morsekit.core.tap.TapState
import `in`.geekofia.morsekit.core.trainer.TrainerMode
import `in`.geekofia.morsekit.core.trainer.TrainerRepository
import `in`.geekofia.morsekit.feature.playback.MorsePlaybackViewModel
import `in`.geekofia.morsekit.platform.PlatformServices
import `in`.geekofia.morsekit.ui.components.ScreenScaffold
import kotlinx.coroutines.delay
import morsekit.shared.generated.resources.Res
import morsekit.shared.generated.resources.ic_close
import morsekit.shared.generated.resources.ic_more_vert
import org.jetbrains.compose.resources.painterResource

/** After a right answer, the next question comes by itself after this pause. */
private const val NEXT_AFTER_RIGHT_MILLIS = 700L

@Composable
fun TrainerRoute(
    platformServices: PlatformServices,
    settingsRepository: SettingsRepository,
    trainerRepository: TrainerRepository,
    reviewPrompter: ReviewPrompter,
    modifier: Modifier = Modifier,
    viewModel: TrainerViewModel = viewModel { TrainerViewModel(trainerRepository, settingsRepository) },
    playbackViewModel: MorsePlaybackViewModel = viewModel {
        MorsePlaybackViewModel(platformServices.audioPlayer, settingsRepository)
    },
) {
    val state = viewModel.uiState
    val settings by settingsRepository.settings.collectAsStateWithLifecycle()
    val keyer = viewModel.keyer
    val codec = MorseCodec()
    val play: (Char) -> Unit = { char -> playbackViewModel.play(codec.encode(char.toString()).message) }
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    var setupSession by rememberSaveable { mutableStateOf(false) }
    var confirmEnd by rememberSaveable { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    // Listen mode plays each new question by itself. Key mode doesn't: hearing it would give the
    // answer away (Hear it is there as a hint).
    LaunchedEffect(state.question, state.mode) {
        if (state.mode == TrainerMode.Listen) play(state.question.target)
    }
    LaunchedEffect(state.answer) {
        state.answer?.let {
            haptics.performHapticFeedback(if (it.correct) HapticFeedbackType.Confirm else HapticFeedbackType.Reject)
        }
    }
    LaunchedEffect(state.answer, state.justUnlocked) {
        if (state.answer?.correct == true && state.justUnlocked == null) {
            delay(NEXT_AFTER_RIGHT_MILLIS)
            viewModel.next()
        }
    }
    LaunchedEffect(state.justUnlocked) {
        state.justUnlocked?.let {
            reviewPrompter.recordSuccessfulUse()
            play(it)
        }
    }
    // Key mode's Timing key, like the Tap screen: wake up exactly when the keyer says so.
    LaunchedEffect(keyer.state, keyer.heldAsDash, state.mode, state.answer) {
        if (state.mode != TrainerMode.Key || state.answer != null) return@LaunchedEffect
        val wait = keyer.millisUntilNextChange() ?: return@LaunchedEffect
        delay(wait)
        viewModel.keyAdvance()
    }
    LaunchedEffect(keyer.heldAsDash) {
        if (keyer.heldAsDash) haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
    }
    DisposableEffect(Unit) { onDispose { playbackViewModel.stop() } }

    val tick = { haptics.performHapticFeedback(HapticFeedbackType.KeyboardTap) }
    // Asks first once something's been answered, since ending shows the summary.
    val endSession = { if (state.session.answered > 0) confirmEnd = true else viewModel.endSession() }
    ScreenScaffold(
        title = { Text("Learn", modifier = Modifier.semantics { heading() }) },
        modifier = modifier,
        actions = {
            // One button, whichever applies: start a session, or end the one under way.
            if (state.session.isCounted) {
                TextButton(onClick = endSession, modifier = Modifier.semantics { contentDescription = "End session" }) {
                    Icon(painterResource(Res.drawable.ic_close), contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("End", modifier = Modifier.padding(start = 4.dp))
                }
            } else {
                TextButton(onClick = { setupSession = true }) { Text("Session") }
            }
            ResetMenu(onReset = { confirmReset = true })
        },
    ) { inner ->
        TrainerScreen(
            state = state,
            keying = KeyingUi(
                state = keyer.state,
                preview = keyer.preview,
                heldAsDash = keyer.heldAsDash,
                tapMode = settings.tapMode,
                onPress = { tick(); viewModel.keyPress() },
                onRelease = viewModel::keyRelease,
                onDot = { tick(); viewModel.keyDot() },
                onDash = { tick(); viewModel.keyDash() },
                onCheck = { tick(); viewModel.keyCheck() },
                onBackspace = viewModel::keyBackspace,
                onTapModeChange = viewModel::setTapMode,
            ),
            onModeChange = viewModel::setMode,
            onPlay = { play(state.question.target) },
            onAnswer = viewModel::answer,
            onNext = viewModel::next,
            onShowMorseChange = { show -> viewModel.setHideMorse(!show) },
            modifier = inner,
        )
    }

    state.justUnlocked?.let { char ->
        NewCharacterDialog(
            char = char,
            onPlay = { play(char) },
            onContinue = {
                viewModel.dismissUnlocked()
                viewModel.next()
            },
        )
    }
    state.summary?.let { summary ->
        SessionSummaryDialog(
            summary = summary,
            onAgain = {
                viewModel.dismissSummary()
                viewModel.startSession(summary.length ?: state.sessionLength)
            },
            onDone = viewModel::dismissSummary,
        )
    }
    if (setupSession) {
        SessionSetupDialog(
            initialLength = state.sessionLength,
            initialMode = state.mode,
            onStart = { length, mode ->
                setupSession = false
                viewModel.setMode(mode)
                viewModel.startSession(length)
            },
            onDismiss = { setupSession = false },
        )
    }
    if (confirmEnd) {
        val length = state.session.length ?: 0
        AlertDialog(
            onDismissRequest = { confirmEnd = false },
            title = { Text("End session?") },
            text = { Text("You've answered ${state.session.answered} of $length. You'll see a summary of those.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmEnd = false
                    viewModel.endSession()
                }) { Text("End") }
            },
            dismissButton = { TextButton(onClick = { confirmEnd = false }) { Text("Keep going") } },
        )
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset progress?") },
            text = { Text("You'll start again with K and M, and your per-character scores are cleared.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    viewModel.resetProgress()
                }) { Text("Reset") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } },
        )
    }
}

/** Stateless: the mode, the level and session, the prompt, the answer area and the feedback. */
@Composable
fun TrainerScreen(
    state: TrainerUiState,
    keying: KeyingUi,
    onModeChange: (TrainerMode) -> Unit,
    onPlay: () -> Unit,
    onAnswer: (Char) -> Unit,
    onNext: () -> Unit,
    onShowMorseChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // A session's mode is chosen when it starts, so the switch only shows between sessions.
        if (!state.session.isCounted) {
            ModeSelector(selected = state.mode, onSelected = onModeChange)
        }
        SessionBar(session = state.session, mode = state.mode)
        LevelCard(progress = state.progress, session = state.session)
        when (state.mode) {
            TrainerMode.Listen -> {
                PromptCard(
                    code = codeOf(state.question.target),
                    hideMorse = state.hideMorse,
                    onPlay = onPlay,
                    onShowMorseChange = onShowMorseChange,
                )
                Choices(state, onAnswer)
            }
            TrainerMode.Key -> KeyArea(target = state.question.target, answered = state.answer != null, keying = keying, onHear = onPlay)
        }
        Feedback(state, onNext)
    }
}

@Composable
private fun ModeSelector(selected: TrainerMode, onSelected: (TrainerMode) -> Unit) {
    val options = TrainerMode.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = mode == selected,
                onClick = { onSelected(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                label = { Text(mode.label) },
            )
        }
    }
}

internal val TrainerMode.label: String
    get() = when (this) {
        TrainerMode.Listen -> "Listen"
        TrainerMode.Key -> "Key"
    }

@Composable
private fun ResetMenu(onReset: () -> Unit) {
    var open by rememberSaveable { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) {
            Icon(painterResource(Res.drawable.ic_more_vert), contentDescription = "More options")
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(text = { Text("Reset progress") }, onClick = {
                open = false
                onReset()
            })
        }
    }
}

/** Everything Key mode needs from the keyer, as plain values and callbacks. */
data class KeyingUi(
    val state: TapState,
    val preview: Char?,
    val heldAsDash: Boolean,
    val tapMode: TapMode,
    val onPress: () -> Unit,
    val onRelease: () -> Unit,
    val onDot: () -> Unit,
    val onDash: () -> Unit,
    val onCheck: () -> Unit,
    val onBackspace: () -> Unit,
    val onTapModeChange: (TapMode) -> Unit,
)

internal fun codeOf(char: Char): String = MorseAlphabet.International.codeFor(char)?.code.orEmpty()
