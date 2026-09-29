package `in`.geekofia.morsekit.ui.components

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import `in`.geekofia.morsekit.core.model.MorseNotation
import `in`.geekofia.morsekit.ui.theme.toMorseStyle

/**
 * Shows canonical Morse (`... --- ...`) with more legible glyphs (`••• −−− •••`).
 *
 * Display only: copy/share should use the canonical string, not what's rendered here.
 */
@Composable
fun MorseDisplay(
    morse: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    style: TextStyle = MaterialTheme.typography.headlineSmall,
) {
    if (morse.isEmpty()) {
        if (placeholder != null) {
            Text(
                text = placeholder,
                modifier = modifier,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    val displayText = remember(morse) { MorseNotation.toDisplayGlyphs(morse) }
    SelectionContainer(modifier = modifier) {
        Text(text = displayText, style = style.toMorseStyle())
    }
}
