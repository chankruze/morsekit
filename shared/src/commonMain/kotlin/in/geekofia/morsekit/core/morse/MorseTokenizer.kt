package `in`.geekofia.morsekit.core.morse

/**
 * Splits already-normalized input into words and letters.
 *
 * Splitting is done by hand with [Char.isWhitespace] instead of regex so the behaviour is
 * identical on the JVM and Kotlin/Native (their `\s` classes differ, e.g. for U+00A0).
 */
object MorseTokenizer {
    private val morseWordBreaks = setOf('/', '|', '\n', '\r')

    /** Plain text words, separated by any run of whitespace. */
    fun textWords(text: String): List<String> {
        val words = mutableListOf<String>()
        val current = StringBuilder()
        for (char in text) {
            if (char.isWhitespace()) {
                if (current.isNotEmpty()) {
                    words += current.toString()
                    current.clear()
                }
            } else {
                current.append(char)
            }
        }
        if (current.isNotEmpty()) words += current.toString()
        return words
    }

    /**
     * Morse words, each a list of raw letter tokens. Tokens are not validated here.
     *
     * - Letters are separated by a single whitespace character.
     * - Words are separated by `/`, `|`, a line break, or two or more consecutive whitespace
     *   characters.
     * - Empty words (e.g. `... / / ---`) are dropped.
     */
    fun morseWords(morse: String): List<List<String>> {
        val words = mutableListOf<List<String>>()
        var letters = mutableListOf<String>()
        val token = StringBuilder()
        var whitespaceRun = 0

        fun endToken() {
            if (token.isNotEmpty()) {
                letters += token.toString()
                token.clear()
            }
        }

        fun endWord() {
            endToken()
            if (letters.isNotEmpty()) {
                words += letters
                letters = mutableListOf()
            }
        }

        for (char in morse) {
            when {
                char in morseWordBreaks -> {
                    endWord()
                    whitespaceRun = 0
                }
                char.isWhitespace() -> {
                    endToken()
                    whitespaceRun++
                    if (whitespaceRun == 2) endWord()
                }
                else -> {
                    whitespaceRun = 0
                    token.append(char)
                }
            }
        }
        endWord()
        return words
    }
}
