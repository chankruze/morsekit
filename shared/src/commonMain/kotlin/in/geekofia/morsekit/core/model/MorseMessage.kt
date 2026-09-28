package `in`.geekofia.morsekit.core.model

/**
 * The code for a single character, e.g. `...` for `S`.
 *
 * Always non-empty and made only of [MorseNotation.DOT] and [MorseNotation.DASH].
 */
data class MorseLetter(val code: String) {
    init {
        require(isValidCode(code)) { "Invalid Morse letter: '$code'" }
    }

    val elements: List<MorseElement>
        get() = code.map { if (it == MorseNotation.DOT) MorseElement.Dot else MorseElement.Dash }

    override fun toString(): String = code

    companion object {
        fun isValidCode(code: String): Boolean =
            code.isNotEmpty() && code.all { it == MorseNotation.DOT || it == MorseNotation.DASH }
    }
}

/** A non-empty sequence of letters forming one word. */
data class MorseWord(val letters: List<MorseLetter>) {
    init {
        require(letters.isNotEmpty()) { "A Morse word must contain at least one letter" }
    }

    override fun toString(): String = letters.joinToString(MorseNotation.LETTER_SEPARATOR)
}

/**
 * A structured Morse message. [toString] renders the canonical notation, e.g. `... --- ...`.
 *
 * Playback features (torch, haptics, audio) consume this structure rather than re-parsing strings.
 */
data class MorseMessage(val words: List<MorseWord>) {
    val isEmpty: Boolean get() = words.isEmpty()

    override fun toString(): String = words.joinToString(MorseNotation.WORD_SEPARATOR)

    companion object {
        val Empty = MorseMessage(emptyList())
    }
}
