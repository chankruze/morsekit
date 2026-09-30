package `in`.geekofia.morsekit.feature.trainer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.geekofia.morsekit.core.trainer.KochProgression
import `in`.geekofia.morsekit.core.trainer.PracticeSession
import `in`.geekofia.morsekit.ui.components.ConfettiBurst
import `in`.geekofia.morsekit.ui.components.MorseDisplay

private const val STREAK_SHOWN_FROM = 3
private const val POP_FROM_SCALE = 0.6f

/** A session this good earns confetti: the same 90% that unlocks a character. */
private const val CELEBRATE_FROM_PERCENT = KochProgression.UNLOCK_CORRECT * 100 / KochProgression.WINDOW

/**
 * One line under the answer area, always the same height so nothing jumps: "Correct!", or what
 * the answer was (and, in Key mode, what was keyed) with Next beside it.
 */
@Composable
internal fun Feedback(state: TrainerUiState, onNext: () -> Unit) {
    val answer = state.answer
    val target = state.question.target
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when {
            answer == null -> Unit
            answer.correct -> CorrectFeedback(streak = state.session.streak, modifier = Modifier.weight(1f))
            else -> {
                Column(modifier = Modifier.weight(1f)) {
                    answer.keyed?.let { keyed ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("You keyed", style = MaterialTheme.typography.bodyMedium)
                            MorseDisplay(morse = keyed, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(if (answer.keyed != null) "$target is" else "It was $target", style = MaterialTheme.typography.titleMedium)
                        MorseDisplay(morse = codeOf(target), style = MaterialTheme.typography.titleMedium)
                    }
                }
                Button(onClick = onNext) { Text("Next") }
            }
        }
    }
}

/** Centered, with a short springy pop; from [STREAK_SHOWN_FROM] in a row, the streak too. */
@Composable
private fun CorrectFeedback(streak: Int, modifier: Modifier = Modifier) {
    val scale = remember { Animatable(POP_FROM_SCALE) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
    }
    Text(
        text = if (streak >= STREAK_SHOWN_FROM) "Correct! · $streak in a row 🔥" else "Correct!",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
    )
}

@Composable
internal fun NewCharacterDialog(char: Char, onPlay: () -> Unit, onContinue: () -> Unit) {
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

/** The end of a counted session: accuracy, misses (most first) and unlocks; Again or Done. */
@Composable
internal fun SessionSummaryDialog(summary: PracticeSession, onAgain: () -> Unit, onDone: () -> Unit) {
    val percent = summary.accuracyPercent ?: 0
    AlertDialog(
        onDismissRequest = onDone,
        title = { Text(if (summary.isFinished) "Session complete" else "Session ended") },
        text = {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("$percent%", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
                    Text("${summary.correct} of ${summary.answered} correct", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = summary.missesByCount.takeIf { it.isNotEmpty() }
                            ?.joinToString(" · ", prefix = "Missed: ") { (char, count) -> if (count > 1) "$char ×$count" else "$char" }
                            ?: "No misses 🎉",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                    if (summary.unlocked.isNotEmpty()) {
                        Text(
                            text = "Unlocked: ${summary.unlocked.joinToString(" ")}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                if (percent >= CELEBRATE_FROM_PERCENT) {
                    ConfettiBurst(modifier = Modifier.matchParentSize(), seed = summary.hashCode())
                }
            }
        },
        confirmButton = { TextButton(onClick = onAgain) { Text("Again") } },
        dismissButton = { TextButton(onClick = onDone) { Text("Done") } },
    )
}
