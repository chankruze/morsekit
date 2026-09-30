package `in`.geekofia.morsekit.feature.tap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * [TapMode.Buttons][in.geekofia.morsekit.core.tap.TapMode.Buttons]: Dot and Dash add elements,
 * Next letter (or [endLetterLabel], e.g. the trainer's Check) and Space (hidden when [onSpace] is
 * null) end them. Nothing depends on timing, so there's no rush, and each button is an ordinary,
 * labelled button for screen readers.
 */
@Composable
fun TapButtons(
    onDot: () -> Unit,
    onDash: () -> Unit,
    onEndLetter: () -> Unit,
    onSpace: (() -> Unit)?,
    modifier: Modifier = Modifier,
    endLetterLabel: String = "Next letter",
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ElementButton(symbol = "•", label = "Dot", onClick = onDot)
            ElementButton(symbol = "−", label = "Dash", onClick = onDash)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(onClick = onEndLetter, modifier = Modifier.weight(1f).heightIn(min = 56.dp)) {
                Text(endLetterLabel)
            }
            if (onSpace != null) {
                FilledTonalButton(onClick = onSpace, modifier = Modifier.weight(1f).heightIn(min = 56.dp)) {
                    Text("Space")
                }
            }
        }
    }
}

@Composable
private fun RowScope.ElementButton(symbol: String, label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.weight(1f).fillMaxHeight().heightIn(min = 96.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(symbol, style = MaterialTheme.typography.displayMedium)
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}
