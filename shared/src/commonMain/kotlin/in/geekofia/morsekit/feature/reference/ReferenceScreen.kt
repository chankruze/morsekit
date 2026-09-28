package `in`.geekofia.morsekit.feature.reference

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import `in`.geekofia.morsekit.ui.components.PlaceholderContent

@Composable
fun ReferenceScreen(modifier: Modifier = Modifier) {
    PlaceholderContent(
        title = "Morse Reference",
        message = "The full Morse alphabet will be listed here.",
        modifier = modifier,
    )
}
