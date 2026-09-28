package `in`.geekofia.morsekit.core.model

/** Canonical textual notation used for all Morse output produced by the app. */
object MorseNotation {
    const val DOT = '.'
    const val DASH = '-'

    /** Placed between the letters of a word, e.g. `... --- ...`. */
    const val LETTER_SEPARATOR = " "

    /** Placed between words, e.g. `.... .. / - .... . .-. .`. */
    const val WORD_SEPARATOR = " / "
}
