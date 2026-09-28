package `in`.geekofia.morsekit.core.morse

import `in`.geekofia.morsekit.core.model.MorseLetter
import `in`.geekofia.morsekit.core.model.MorseMessage
import `in`.geekofia.morsekit.core.model.MorseWord

/**
 * Stateless, thread-safe translator between text and Morse code.
 *
 * Inputs are normalized first (see [MorseNormalizer]); invalid input never throws but is
 * reported through [EncodeResult.issues] / [DecodeResult.issues].
 */
class MorseCodec(private val alphabet: MorseAlphabet = MorseAlphabet.International) {

    /** `"SOS"` → `"... --- ..."`. Unsupported characters are skipped and reported. */
    fun encode(text: String): EncodeResult {
        val issues = LinkedHashSet<TranslationIssue>()
        val words = MorseTokenizer.textWords(text.mapChars(MorseNormalizer::normalizeTextChar))
            .mapNotNull { word ->
                val letters = mutableListOf<MorseLetter>()
                word.forEachCodePoint { symbol ->
                    val code = symbol.singleOrNull()?.let(alphabet::codeFor)
                    if (code != null) letters += code else issues += TranslationIssue.UnsupportedCharacter(symbol)
                }
                if (letters.isEmpty()) null else MorseWord(letters)
            }
        return EncodeResult(MorseMessage(words), issues.toList())
    }

    /** `"... --- ..."` → `"SOS"`. Unreadable letters become [REPLACEMENT_CHAR] and are reported. */
    fun decode(morse: String): DecodeResult {
        val issues = LinkedHashSet<TranslationIssue>()
        val text = MorseTokenizer.morseWords(morse.mapChars(MorseNormalizer::normalizeMorseChar))
            .joinToString(" ") { tokens ->
                tokens.joinToString("") { token ->
                    val char = decodeToken(token)
                    if (char == null) issues += issueFor(token)
                    (char ?: REPLACEMENT_CHAR).toString()
                }
            }
        return DecodeResult(text, issues.toList())
    }

    /** `'s'` → `"..."`, or `null` if the character is unsupported. */
    fun encodeChar(char: Char): String? = alphabet.codeFor(MorseNormalizer.normalizeTextChar(char))?.code

    /** `"..."` → `'S'`, or `null` if the symbol isn't a known code. Surrounding whitespace is ignored. */
    fun decodeSymbol(symbol: String): Char? =
        decodeToken(symbol.trim().mapChars(MorseNormalizer::normalizeMorseChar))

    fun isEncodable(text: String): Boolean = encode(text).isValid

    fun isValidMorse(morse: String): Boolean = decode(morse).isValid

    /**
     * Parses Morse into a [MorseMessage], e.g. for playback. Every well-formed dot/dash letter is
     * kept, even one that isn't in the alphabet; malformed tokens (`-x-`) are dropped.
     */
    fun parse(morse: String): MorseMessage = MorseMessage(
        MorseTokenizer.morseWords(morse.mapChars(MorseNormalizer::normalizeMorseChar)).mapNotNull { tokens ->
            val letters = tokens.filter { MorseLetter.isValidCode(it) }.map(::MorseLetter)
            if (letters.isEmpty()) null else MorseWord(letters)
        },
    )

    private fun decodeToken(token: String): Char? =
        if (MorseLetter.isValidCode(token)) alphabet.characterFor(MorseLetter(token)) else null

    private fun issueFor(token: String): TranslationIssue =
        if (MorseLetter.isValidCode(token)) {
            TranslationIssue.UnknownCode(token)
        } else {
            TranslationIssue.MalformedCode(token)
        }

    /** Iterates by code point so a surrogate pair (e.g. an emoji) is reported as one symbol. */
    private inline fun String.forEachCodePoint(action: (String) -> Unit) {
        var start = 0
        while (start < length) {
            val isPair = this[start].isHighSurrogate() && start + 1 < length && this[start + 1].isLowSurrogate()
            val end = if (isPair) start + 2 else start + 1
            action(substring(start, end))
            start = end
        }
    }

    companion object {
        /** Stands in for a Morse letter that couldn't be decoded. */
        const val REPLACEMENT_CHAR = '\uFFFD'
    }
}
