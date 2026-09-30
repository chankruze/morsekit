package `in`.geekofia.morsekit.feature.trainer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import `in`.geekofia.morsekit.ui.components.MorseDisplay
import morsekit.shared.generated.resources.Res
import morsekit.shared.generated.resources.ic_visibility
import morsekit.shared.generated.resources.ic_visibility_off
import morsekit.shared.generated.resources.ic_volume
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** Listen mode's prompt, one compact row: play, the Morse (or a hint when it's hidden), and the show/hide toggle, each labelled. */
@Composable
internal fun PromptCard(
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
internal fun LabeledAction(icon: DrawableResource, label: String, modifier: Modifier = Modifier) {
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
internal fun Choices(state: TrainerUiState, onAnswer: (Char) -> Unit) {
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
