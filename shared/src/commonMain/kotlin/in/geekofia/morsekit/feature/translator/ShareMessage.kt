package `in`.geekofia.morsekit.feature.translator

import `in`.geekofia.morsekit.core.model.MorseNotation
import `in`.geekofia.morsekit.core.model.TranslationDirection
import `in`.geekofia.morsekit.platform.StoreListing

/** Where recipients can get MorseKit. Resolves once the app is published on Google Play. */
internal const val MORSEKIT_STORE_URL = StoreListing.PLAY_URL

/**
 * The text sent by Share, or `null` if there's nothing to share.
 *
 * Text → Morse is a puzzle: the Morse in legible glyphs, plus plain dots and dashes that any
 * decoder accepts, and an invitation to decode it with MorseKit. Morse → Text is the reveal:
 * the decoded text with the (canonical) Morse it came from. Copy stays raw; only Share uses this.
 */
internal fun shareMessage(state: TranslatorUiState): String? {
    if (!state.hasOutput) return null
    return when (state.direction) {
        TranslationDirection.TextToMorse -> buildString {
            appendLine("🕵️ Can you decode this secret message?")
            appendLine()
            appendLine(MorseNotation.toDisplayGlyphs(state.output))
            appendLine()
            appendLine("(${state.output} in plain dots and dashes)")
            appendLine()
            appendLine("Stuck? Decode it, and learn Morse, with MorseKit 📡")
            append("Install it now 👉 $MORSEKIT_STORE_URL")
        }
        TranslationDirection.MorseToText -> buildString {
            appendLine("📡 I decoded a Morse message with MorseKit:")
            appendLine()
            appendLine("\"${state.output}\"")
            appendLine()
            appendLine(state.message.toString())
            appendLine()
            appendLine("Decode your own, and learn Morse, with MorseKit.")
            append("Install it now 👉 $MORSEKIT_STORE_URL")
        }
    }
}
