package `in`.geekofia.morsekit.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.em

/** Monospace variant used wherever raw Morse is shown or edited, so dots and dashes line up. */
fun TextStyle.toMorseStyle(): TextStyle =
    copy(fontFamily = FontFamily.Monospace, letterSpacing = 0.1.em)
