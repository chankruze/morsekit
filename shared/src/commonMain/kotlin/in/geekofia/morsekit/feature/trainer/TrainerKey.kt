package `in`.geekofia.morsekit.feature.trainer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import `in`.geekofia.morsekit.core.tap.TapMode
import `in`.geekofia.morsekit.feature.tap.MorseKey
import `in`.geekofia.morsekit.feature.tap.TapButtons
import `in`.geekofia.morsekit.ui.components.MorseDisplay
import morsekit.shared.generated.resources.Res
import morsekit.shared.generated.resources.ic_backspace
import morsekit.shared.generated.resources.ic_volume

/** The key needs a fixed height here: the screen scrolls, so it can't fill the rest of it. */
private val KEY_HEIGHT = 180.dp

/**
 * Key mode: the character to key, what's been keyed so far (and what it decodes to), then the
 * Tap screen's key or buttons. Timing mode grades when the letter's pause ends; Buttons mode
 * grades on Check.
 */
@Composable
internal fun KeyArea(target: Char, answered: Boolean, keying: KeyingUi, onHear: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        KeyPrompt(target, answered, keying, onHear)
        val keyModifier = Modifier.fillMaxWidth().height(KEY_HEIGHT)
        when (keying.tapMode) {
            TapMode.Timing -> MorseKey(
                pressed = keying.state.isPressed,
                heldAsDash = keying.heldAsDash,
                onPress = keying.onPress,
                onRelease = keying.onRelease,
                onDot = keying.onDot,
                onDash = keying.onDash,
                onEndLetter = keying.onCheck,
                onSpace = keying.onCheck,
                modifier = keyModifier,
            )
            TapMode.Buttons -> TapButtons(
                onDot = keying.onDot,
                onDash = keying.onDash,
                onEndLetter = keying.onCheck,
                onSpace = null,
                endLetterLabel = "Check",
                modifier = keyModifier,
            )
        }
        InputSwitch(keying.tapMode, keying.onTapModeChange)
    }
}

@Composable
private fun KeyPrompt(target: Char, answered: Boolean, keying: KeyingUi, onHear: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabeledAction(
                icon = Res.drawable.ic_volume,
                label = "Hear it",
                modifier = Modifier.clickable(role = Role.Button, onClick = onHear),
            )
            Column(
                modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(target.toString(), style = MaterialTheme.typography.displayMedium)
                val letter = keying.state.currentLetter
                if (letter.isEmpty()) {
                    Text(
                        text = if (answered) " " else "Key its Morse",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MorseDisplay(morse = letter, style = MaterialTheme.typography.titleLarge)
                        Text("→ ${keying.preview ?: "?"}", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
            LabeledAction(
                icon = Res.drawable.ic_backspace,
                label = "Delete",
                modifier = Modifier.clickable(
                    enabled = !answered && keying.state.currentLetter.isNotEmpty(),
                    role = Role.Button,
                    onClick = keying.onBackspace,
                ),
            )
        }
    }
}

/** Key mode uses the Tap screen's input mode; this switches it without leaving Learn. */
@Composable
private fun InputSwitch(mode: TapMode, onChange: (TapMode) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (mode == TapMode.Timing) "Tap for a dot, hold for a dash" else "Tap Dot and Dash, then Check",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = { onChange(if (mode == TapMode.Timing) TapMode.Buttons else TapMode.Timing) }) {
            Text(if (mode == TapMode.Timing) "Use buttons" else "Use timing")
        }
    }
}
