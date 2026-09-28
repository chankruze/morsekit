package `in`.geekofia.morsekit.core.morse

import `in`.geekofia.morsekit.core.model.MorseNotation

/**
 * Cleans up user input before translation.
 *
 * Mobile keyboards frequently substitute "smart" punctuation, and Morse copied from other
 * sources often uses typographic dots and dashes. Both are folded to the canonical forms here.
 */
object MorseNormalizer {
    private val alternativeDots = setOf('·', '•', '∙', '⋅')
    private val alternativeDashes = setOf('−', '–', '—', '‒', '_')
    private val singleQuotes = setOf('‘', '’', '‚', '′')
    private val doubleQuotes = setOf('“', '”', '„', '″')

    /** Upper-cases ASCII letters and folds smart quotes. Other characters pass through unchanged. */
    fun normalizeTextChar(char: Char): Char = when (char) {
        in 'a'..'z' -> char.uppercaseChar()
        in singleQuotes -> '\''
        in doubleQuotes -> '"'
        else -> char
    }

    /** Folds typographic dots and dashes to [MorseNotation.DOT] and [MorseNotation.DASH]. */
    fun normalizeMorseChar(char: Char): Char = when (char) {
        in alternativeDots -> MorseNotation.DOT
        in alternativeDashes -> MorseNotation.DASH
        else -> char
    }

    /** E.g. `"  hello   world "` → `"HELLO WORLD"`. */
    fun normalizeText(text: String): String =
        MorseTokenizer.textWords(text.mapChars(::normalizeTextChar)).joinToString(" ")

    /** E.g. `"...   ---|•••"` → `"... / --- / ..."`. Invalid tokens are kept as-is. */
    fun normalizeMorse(morse: String): String =
        MorseTokenizer.morseWords(morse.mapChars(::normalizeMorseChar))
            .joinToString(MorseNotation.WORD_SEPARATOR) { it.joinToString(MorseNotation.LETTER_SEPARATOR) }
}

internal inline fun String.mapChars(transform: (Char) -> Char): String =
    buildString(length) { this@mapChars.forEach { append(transform(it)) } }
