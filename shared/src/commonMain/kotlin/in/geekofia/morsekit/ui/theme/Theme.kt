package `in`.geekofia.morsekit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

enum class ThemeMode { System, Light, Dark }

@Composable
fun MorseKitTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit,
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColors else LightColors,
        content = content,
    )
}
