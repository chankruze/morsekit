package `in`.geekofia.morsekit.core.model

/** The two signal elements every Morse code is built from. */
enum class MorseElement(val symbol: Char) {
    Dot(MorseNotation.DOT),
    Dash(MorseNotation.DASH);

    companion object {
        fun fromSymbol(symbol: Char): MorseElement? = when (symbol) {
            MorseNotation.DOT -> Dot
            MorseNotation.DASH -> Dash
            else -> null
        }
    }
}
