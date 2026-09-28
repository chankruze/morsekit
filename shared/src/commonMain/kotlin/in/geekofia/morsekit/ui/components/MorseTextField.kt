package `in`.geekofia.morsekit.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import `in`.geekofia.morsekit.ui.theme.toMorseStyle

/**
 * Multi-line text input for either plain text or Morse.
 *
 * With [isMorse], the field uses a monospace style and disables auto-correct so the keyboard
 * doesn't rewrite dots and dashes (e.g. iOS turning `--` into `—`).
 */
@Composable
fun MorseTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isMorse: Boolean = false,
    placeholder: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    minLines: Int = 3,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        supportingText = supportingText?.let { { Text(it) } },
        isError = isError,
        minLines = minLines,
        textStyle = if (isMorse) LocalTextStyle.current.toMorseStyle() else LocalTextStyle.current,
        keyboardOptions = if (isMorse) {
            KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Ascii,
            )
        } else {
            KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                autoCorrectEnabled = false,
            )
        },
    )
}
