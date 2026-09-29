package `in`.geekofia.morsekit.core.model

/** Canonical textual notation used for all Morse output produced by the app. */
object MorseNotation {
    const val DOT = '.'
    const val DASH = '-'

    /** Placed between the letters of a word, e.g. `... --- ...`. */
    const val LETTER_SEPARATOR = " "

    /** Placed between words, e.g. `.... .. / - .... . .-. .`. */
    const val WORD_SEPARATOR = " / "

    /** Bigger, more legible glyphs for showing Morse to people (`••• −−− •••`). Display only. */
    const val DISPLAY_DOT = '•'
    const val DISPLAY_DASH = '−'

    /** Canonical Morse with [DOT]/[DASH] swapped for [DISPLAY_DOT]/[DISPLAY_DASH]. */
    fun toDisplayGlyphs(morse: String): String = morse.map {
        when (it) {
            DOT -> DISPLAY_DOT
            DASH -> DISPLAY_DASH
            else -> it
        }
    }.joinToString("")
}
