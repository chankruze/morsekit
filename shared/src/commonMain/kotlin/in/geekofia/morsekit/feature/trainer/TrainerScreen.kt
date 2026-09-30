package `in`.geekofia.morsekit.feature.trainer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.geekofia.morsekit.core.morse.MorseAlphabet
import `in`.geekofia.morsekit.core.morse.MorseCodec
import `in`.geekofia.morsekit.core.review.ReviewPrompter
import `in`.geekofia.morsekit.core.settings.SettingsRepository
import `in`.geekofia.morsekit.core.trainer.KochOrder
import `in`.geekofia.morsekit.core.trainer.KochProgression
import `in`.geekofia.morsekit.core.trainer.TrainerProgress
import `in`.geekofia.morsekit.core.trainer.TrainerRepository
import `in`.geekofia.morsekit.feature.playback.MorsePlaybackViewModel
import `in`.geekofia.morsekit.platform.PlatformServices
import `in`.geekofia.morsekit.ui.components.ConfettiBurst
import `in`.geekofia.morsekit.ui.components.MorseDisplay
import `in`.geekofia.morsekit.ui.components.ScreenScaffold
import kotlinx.coroutines.delay
import morsekit.shared.generated.resources.Res
import morsekit.shared.generated.resources.ic_more_vert
import morsekit.shared.generated.resources.ic_visibility
import morsekit.shared.generated.resources.ic_visibility_off
import morsekit.shared.generated.resources.ic_volume
import org.jetbrains.compose.resources.DrawableResource
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
    viewModel: TrainerViewModel = viewModel { TrainerViewModel(trainerRepository) },
    playbackViewModel: MorsePlaybackViewModel = viewModel {
        MorsePlaybackViewModel(platformServices.audioPlayer, settingsRepository)
    },
) {
    val state = viewModel.uiState
    val codec = MorseCodec()
    val play: (Char) -> Unit = { char -> playbackViewModel.play(codec.encode(char.toString()).message) }
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    // Each new question plays once by itself; Play again replays it.
    LaunchedEffect(state.question) { play(state.question.target) }
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
    DisposableEffect(Unit) { onDispose { playbackViewModel.stop() } }

    ScreenScaffold(
        title = { Text("Learn", modifier = Modifier.semantics { heading() }) },
        modifier = modifier,
        actions = { ResetMenu(onReset = { confirmReset = true }) },
    ) { inner ->
        TrainerScreen(
            state = state,
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

/** Stateless: the level, the prompt, the choices and the feedback. */
@Composable
fun TrainerScreen(
    state: TrainerUiState,
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
        LevelCard(state.progress, state.session)
        PromptCard(
            code = codeOf(state.question.target),
            hideMorse = state.hideMorse,
            onPlay = onPlay,
            onShowMorseChange = onShowMorseChange,
        )
        Choices(state, onAnswer)
        Feedback(state, onNext)
    }
}

/** The level and session score on one line, the unlocked characters on another, then the bar. */
@Composable
private fun LevelCard(progress: TrainerProgress, session: SessionScore) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Level ${progress.level} of ${KochOrder.characters.size}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f).semantics { heading() },
                )
                if (session.answered > 0) {
                    Text(
                        text = "${session.correct}/${session.answered} · streak ${session.streak}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.semantics {
                            stateDescription = "This session: ${session.correct} of ${session.answered} right, streak ${session.streak}"
                        },
                    )
                }
            }
            UnlockedCharacters(progress)
            if (progress.isComplete) {
                Text("All characters unlocked. Keep practising the ones you miss.", style = MaterialTheme.typography.bodySmall)
            } else {
                LinearProgressIndicator(
                    progress = { (progress.recentCorrect.toFloat() / KochProgression.UNLOCK_CORRECT).coerceAtMost(1f) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "${progress.recentCorrect} of the last ${progress.recent.size} right · " +
                        "${KochProgression.UNLOCK_CORRECT}/${KochProgression.WINDOW} unlocks the next",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * One line, whatever the level: it scrolls sideways and keeps the newest (bold) character in
 * view, so the card doesn't grow as characters are unlocked.
 */
@Composable
private fun UnlockedCharacters(progress: TrainerProgress) {
    val scroll = rememberScrollState()
    LaunchedEffect(progress.level) { scroll.animateScrollTo(scroll.maxValue) }
    Text(
        text = buildAnnotatedString {
            progress.unlocked.forEachIndexed { i, char ->
                if (i > 0) append("  ")
                if (char == progress.newest) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                        append(char)
                    }
                } else {
                    append(char)
                }
            }
        },
        style = MaterialTheme.typography.titleMedium,
        maxLines = 1,
        softWrap = false,
        modifier = Modifier.horizontalScroll(scroll),
    )
}

/** One compact row: play, the Morse (or a hint when it's hidden), and the show/hide toggle, each labelled. */
@Composable
private fun PromptCard(
    code: String,
    hideMorse: Boolean,
    onPlay: () -> Unit,
    onShowMorseChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabeledAction(
                icon = Res.drawable.ic_volume,
                label = "Play again",
                modifier = Modifier.clickable(role = Role.Button, onClick = onPlay),
            )
            Box(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                if (hideMorse) {
                    Text("Listen, then pick", style = MaterialTheme.typography.titleMedium)
                } else {
                    MorseDisplay(morse = code, style = MaterialTheme.typography.headlineMedium)
                }
            }
            LabeledAction(
                icon = if (hideMorse) Res.drawable.ic_visibility_off else Res.drawable.ic_visibility,
                label = "Show Morse",
                modifier = Modifier.toggleable(value = !hideMorse, role = Role.Switch, onValueChange = onShowMorseChange),
            )
        }
    }
}

/**
 * An icon with a small label under it, as one touch target (at least 64 × 56 dp): the caller adds
 * `clickable` or `toggleable`, so a screen reader reads the label once, with its role and state.
 */
@Composable
private fun LabeledAction(icon: DrawableResource, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .then(modifier)
            .sizeIn(minWidth = 64.dp, minHeight = 56.dp)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        Icon(painterResource(icon), contentDescription = null)
        Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

/** Two columns of large buttons. After answering, the right one and a wrong pick are marked. */
@Composable
private fun Choices(state: TrainerUiState, onAnswer: (Char) -> Unit) {
    val answer = state.answer
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        state.question.choices.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { choice ->
                    val isTarget = choice == state.question.target
                    val modifier = Modifier.weight(1f).heightIn(min = 64.dp)
                    val label: @Composable () -> Unit = {
                        Text(
                            text = when {
                                answer != null && isTarget -> "✓ $choice"
                                answer?.chosen == choice -> "✗ $choice"
                                else -> choice.toString()
                            },
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }
                    when {
                        answer != null && isTarget -> Button(onClick = {}, modifier = modifier) { label() }
                        answer?.chosen == choice -> Button(
                            onClick = {},
                            modifier = modifier,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ),
                        ) { label() }
                        else -> OutlinedButton(onClick = { onAnswer(choice) }, enabled = answer == null, modifier = modifier) {
                            label()
                        }
                    }
                }
                if (row.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}

/**
 * One line under the choices, always the same height so nothing jumps: "Right!", or the answer
 * with its Morse and Next beside it.
 */
@Composable
private fun Feedback(state: TrainerUiState, onNext: () -> Unit) {
    val answer = state.answer
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when {
            answer == null -> Unit
            answer.correct -> RightFeedback(streak = state.session.streak, modifier = Modifier.weight(1f))
            else -> {
                Text("It was ${state.question.target}", style = MaterialTheme.typography.titleMedium)
                MorseDisplay(
                    morse = codeOf(state.question.target),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = onNext) { Text("Next") }
            }
        }
    }
}

/** Centered, with a short springy pop; from [STREAK_SHOWN_FROM] in a row, the streak too. */
@Composable
private fun RightFeedback(streak: Int, modifier: Modifier = Modifier) {
    val scale = remember { Animatable(POP_FROM_SCALE) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
    }
    Text(
        text = if (streak >= STREAK_SHOWN_FROM) "Right! · $streak in a row 🔥" else "Right!",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
    )
}

private const val STREAK_SHOWN_FROM = 3
private const val POP_FROM_SCALE = 0.6f

@Composable
private fun NewCharacterDialog(char: Char, onPlay: () -> Unit, onContinue: () -> Unit) {
    AlertDialog(
        onDismissRequest = onContinue,
        title = { Text("New character: $char") },
        text = {
            // The confetti is drawn over the content, but decorative: it has no semantics.
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(char.toString(), style = MaterialTheme.typography.displayMedium)
                    MorseDisplay(morse = codeOf(char), style = MaterialTheme.typography.headlineMedium)
                    Text("You'll hear it often in the next questions.", style = MaterialTheme.typography.bodyMedium)
                }
                ConfettiBurst(modifier = Modifier.matchParentSize(), seed = char.code)
            }
        },
        confirmButton = { TextButton(onClick = onContinue) { Text("Continue") } },
        dismissButton = { TextButton(onClick = onPlay) { Text("Play") } },
    )
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

private fun codeOf(char: Char): String = MorseAlphabet.International.codeFor(char)?.code.orEmpty()
