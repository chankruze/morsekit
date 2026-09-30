package `in`.geekofia.morsekit.feature.trainer

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import `in`.geekofia.morsekit.core.trainer.KochOrder
import `in`.geekofia.morsekit.core.trainer.KochProgression
import `in`.geekofia.morsekit.core.trainer.PracticeSession
import `in`.geekofia.morsekit.core.trainer.TrainerMode
import `in`.geekofia.morsekit.core.trainer.TrainerProgress

/**
 * During a counted session only, information only: the session's mode and question number on the
 * left, the correct count on the right, and a line showing how far along it is. The mode is fixed
 * for the session (the Listen | Key switch is hidden meanwhile), so it's named here instead.
 * Starting and ending are the top bar's one button (Session / ✕ End). Endless practice shows
 * nothing here, so the screen stays as simple as without sessions.
 */
@Composable
internal fun SessionBar(session: PracticeSession, mode: TrainerMode) {
    val length = session.length ?: return
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${mode.label} · Question ${(session.answered + 1).coerceAtMost(length)} of $length",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${session.correct} correct",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LinearProgressIndicator(
            progress = { session.answered.toFloat() / length },
            modifier = Modifier.fillMaxWidth().semantics {
                stateDescription = "${session.answered} of $length answered"
            },
        )
    }
}

/**
 * The unlocked characters, with the level (small, top right) and, during endless practice, the
 * score so far under it (a session shows its own in [SessionBar]); then how close the next
 * unlock is.
 */
@Composable
internal fun LevelCard(progress: TrainerProgress, session: PracticeSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(modifier = Modifier.weight(1f)) { UnlockedCharacters(progress) }
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = "Level ${progress.level} of ${KochOrder.characters.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.semantics { heading() },
                    )
                    if (!session.isCounted && session.answered > 0) {
                        Text(
                            text = "${session.correct}/${session.answered} · 🔥 ${session.streak}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.semantics {
                                stateDescription = "${session.correct} of ${session.answered} correct, streak ${session.streak}"
                            },
                        )
                    }
                }
            }
            if (progress.isComplete) {
                Text(
                    text = "All characters unlocked. Keep practising the ones you miss.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LinearProgressIndicator(
                    progress = { (progress.recentCorrect.toFloat() / KochProgression.UNLOCK_CORRECT).coerceAtMost(1f) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "${progress.recentCorrect} of ${KochProgression.UNLOCK_CORRECT} correct to unlock the next " +
                        "(in your last ${KochProgression.WINDOW} answers)",
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
