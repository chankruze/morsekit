package `in`.geekofia.morsekit.core.morse

import `in`.geekofia.morsekit.core.model.MorseLetter

enum class MorseCategory {
    Letter,
    Digit,
    Punctuation,

    /** Not an alphabet character: see [MorseProsigns]. [MorseMapping.category] never returns it. */
    Prosign,
}

data class MorseMapping(val character: Char, val code: MorseLetter) {
    val category: MorseCategory
        get() = when {
            character.isLetter() -> MorseCategory.Letter
            character.isDigit() -> MorseCategory.Digit
            else -> MorseCategory.Punctuation
        }
}

/**
 * An immutable, bidirectional character ↔ code table.
 *
 * Characters are stored in upper case. Case folding and other input clean-up are the job of
 * [MorseNormalizer], so lookups here are exact.
 */
class MorseAlphabet(val mappings: List<MorseMapping>) {
    private val codeByChar: Map<Char, MorseLetter> = mappings.associate { it.character to it.code }
    private val charByCode: Map<MorseLetter, Char> = mappings.associate { it.code to it.character }

    init {
        require(codeByChar.size == mappings.size) { "Alphabet contains duplicate characters" }
        require(charByCode.size == mappings.size) { "Alphabet contains duplicate codes" }
    }

    val characters: Set<Char> get() = codeByChar.keys

    fun codeFor(character: Char): MorseLetter? = codeByChar[character]

    fun characterFor(code: MorseLetter): Char? = charByCode[code]

    operator fun contains(character: Char): Boolean = character in codeByChar

    companion object {
        /**
         * ITU-R M.1677-1 letters, digits and punctuation, plus widely used non-ITU
         * punctuation (`! & ; _ $`).
         */
        val International: MorseAlphabet = alphabetOf(
            'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..", 'E' to ".",
            'F' to "..-.", 'G' to "--.", 'H' to "....", 'I' to "..", 'J' to ".---",
            'K' to "-.-", 'L' to ".-..", 'M' to "--", 'N' to "-.", 'O' to "---",
            'P' to ".--.", 'Q' to "--.-", 'R' to ".-.", 'S' to "...", 'T' to "-",
            'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-", 'Y' to "-.--",
            'Z' to "--..",

            '0' to "-----", '1' to ".----", '2' to "..---", '3' to "...--", '4' to "....-",
            '5' to ".....", '6' to "-....", '7' to "--...", '8' to "---..", '9' to "----.",

            '.' to ".-.-.-", ',' to "--..--", '?' to "..--..", '\'' to ".----.",
            '/' to "-..-.", '(' to "-.--.", ')' to "-.--.-", ':' to "---...",
            '=' to "-...-", '+' to ".-.-.", '-' to "-....-", '"' to ".-..-.",
            '@' to ".--.-.",

            // Non-ITU, but in common use.
            '!' to "-.-.--", '&' to ".-...", ';' to "-.-.-.", '_' to "..--.-", '$' to "...-..-",
        )

        private fun alphabetOf(vararg entries: Pair<Char, String>): MorseAlphabet =
            MorseAlphabet(entries.map { (char, code) -> MorseMapping(char, MorseLetter(code)) })
    }
}
