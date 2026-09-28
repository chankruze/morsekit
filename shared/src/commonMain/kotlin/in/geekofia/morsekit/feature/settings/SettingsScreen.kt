package `in`.geekofia.morsekit.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import `in`.geekofia.morsekit.ui.components.PlaceholderContent

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    PlaceholderContent(
        title = "Settings",
        message = "Theme, playback speed and other preferences will live here.",
        modifier = modifier,
    )
}
