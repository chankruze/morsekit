package `in`.geekofia.morsekit.feature.trainer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.geekofia.morsekit.core.trainer.PracticeSession
import `in`.geekofia.morsekit.core.trainer.TrainerMode

/**
 * Configures and confirms a session before it starts: what it is, how long, and which mode.
 * Starts with the last length chosen and the current mode.
 */
@Composable
internal fun SessionSetupDialog(
    initialLength: Int,
    initialMode: TrainerMode,
    onStart: (length: Int, mode: TrainerMode) -> Unit,
    onDismiss: () -> Unit,
) {
    var length by rememberSaveable { mutableIntStateOf(initialLength) }
    var mode by rememberSaveable { mutableStateOf(initialMode) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start a session") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Answer a set number of questions, then see how you did: your accuracy, the characters " +
                        "you missed, and anything you unlocked.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text("Questions", style = MaterialTheme.typography.labelLarge)
                Segmented(
                    options = PracticeSession.LENGTHS,
                    selected = length,
                    label = { "$it" },
                    onSelected = { length = it },
                )
                Text("Mode", style = MaterialTheme.typography.labelLarge)
                Segmented(
                    options = TrainerMode.entries,
                    selected = mode,
                    label = { if (it == TrainerMode.Listen) "Listen" else "Key" },
                    onSelected = { mode = it },
                )
            }
        },
        confirmButton = { TextButton(onClick = { onStart(length, mode) }) { Text("Start") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun <T> Segmented(options: List<T>, selected: T, label: (T) -> String, onSelected: (T) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelected(option) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                label = { Text(label(option)) },
            )
        }
    }
}
