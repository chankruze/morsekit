package `in`.geekofia.morsekit.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import `in`.geekofia.morsekit.ui.theme.toMorseStyle

/**
 * Multi-line text input for either plain text or Morse.
 *
 * With [isMorse], the field uses a monospace style and disables auto-correct so the keyboard
 * doesn't rewrite dots and dashes (e.g. iOS turning `--` into `—`).
 *
 * [borderless] drops the outline, underline and background, for fields that sit inside a card
 * that already provides the frame. It then has no floating label; the card should title it
 * ([label] is still what screen readers announce).
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
    borderless: Boolean = false,
) {
    val textStyle = if (isMorse) LocalTextStyle.current.toMorseStyle() else LocalTextStyle.current
    val keyboardOptions = keyboardOptionsFor(isMorse)
    if (borderless) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            // No visible label here, so screen readers get it this way; the placeholder goes once typed.
            modifier = modifier.fillMaxWidth().semantics { contentDescription = label },
            placeholder = placeholder?.let { { Text(it) } },
            supportingText = supportingText?.let { { Text(it) } },
            isError = isError,
            minLines = minLines,
            textStyle = textStyle,
            keyboardOptions = keyboardOptions,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
            ),
        )
        return
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        supportingText = supportingText?.let { { Text(it) } },
        isError = isError,
        minLines = minLines,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
    )
}

private fun keyboardOptionsFor(isMorse: Boolean): KeyboardOptions =
    if (isMorse) {
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
    }
