package `in`.geekofia.morsekit.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import morsekit.shared.generated.resources.Res
import morsekit.shared.generated.resources.space_grotesk
import org.jetbrains.compose.resources.Font

/** Monospace variant used wherever raw Morse is shown or edited, so dots and dashes line up. */
fun TextStyle.toMorseStyle(): TextStyle =
    copy(fontFamily = FontFamily.Monospace, letterSpacing = 0.1.em)

/**
 * Space Grotesk, a variable font (weight axis 300..700) bundled as a Compose resource so it
 * renders the same on Android and iOS. The file's default weight is 300 (Light); each [Font]
 * below sets the axis to its weight through its variation settings.
 */
@Composable
fun spaceGroteskFontFamily(): FontFamily = FontFamily(
    Font(Res.font.space_grotesk, FontWeight.Normal),
    Font(Res.font.space_grotesk, FontWeight.Medium),
    Font(Res.font.space_grotesk, FontWeight.SemiBold),
)
