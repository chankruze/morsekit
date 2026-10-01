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
class MorseCodec(
    private val alphabet: MorseAlphabet = MorseAlphabet.International,
    prosigns: List<MorseProsign> = MorseProsigns.Common,
) {
    private val prosignsByLetters: Map<String, MorseProsign> = prosigns.associateBy { it.letters }

    /**
     * Prosigns the decoder writes as `<SOS>`. Those whose code is also a character (AR is `+`, BT
     * `=`, KN `(`, AS `&`) are left out: the alphabet wins, so text that decodes today doesn't change.
     */
    private val prosignsByCode: Map<MorseLetter, MorseProsign> =
        prosigns.filter { alphabet.characterFor(it.code) == null }.associateBy { it.code }

    /**
     * `"SOS"` → `"... --- ..."`. Unsupported characters are skipped and reported. A prosign
     * written in angle brackets (`<SOS>`) becomes one letter, its letters run together
     * (`...---...`), so a decoded prosign survives a swap back to Morse.
     */
    fun encode(text: String): EncodeResult {
        val issues = LinkedHashSet<TranslationIssue>()
        val words = MorseTokenizer.textWords(text.mapChars(MorseNormalizer::normalizeTextChar))
            .mapNotNull { word ->
                val letters = mutableListOf<MorseLetter>()
                var rest = word
                while (rest.isNotEmpty()) {
                    val prosign = leadingProsign(rest)
                    if (prosign != null) {
                        letters += prosign.code
                        rest = rest.substring(prosign.letters.length + 2)
                        continue
                    }
                    val symbol = rest.firstCodePoint()
                    val code = symbol.singleOrNull()?.let(alphabet::codeFor)
                    if (code != null) letters += code else issues += TranslationIssue.UnsupportedCharacter(symbol)
                    rest = rest.substring(symbol.length)
                }
                if (letters.isEmpty()) null else MorseWord(letters)
            }
        return EncodeResult(MorseMessage(words), issues.toList())
    }

    /**
     * `"... --- ..."` → `"SOS"`. Unreadable letters become [REPLACEMENT_CHAR] and are reported. A
     * code that's no character but a prosign is written `<SOS>` (`...---...`).
     */
    fun decode(morse: String): DecodeResult {
        val issues = LinkedHashSet<TranslationIssue>()
        val text = MorseTokenizer.morseWords(morse.mapChars(MorseNormalizer::normalizeMorseChar))
            .joinToString(" ") { tokens ->
                tokens.joinToString("") { token ->
                    decodeToken(token)?.toString()
                        ?: prosignFor(token)?.let { "<${it.letters}>" }
                        ?: run {
                            issues += issueFor(token)
                            REPLACEMENT_CHAR.toString()
                        }
                }
            }
        return DecodeResult(text, issues.toList())
    }

    /** The prosign written in angle brackets at the start of [text] (`<SOS>…`), if any. */
    private fun leadingProsign(text: String): MorseProsign? {
        if (!text.startsWith('<')) return null
        val end = text.indexOf('>')
        if (end < 0) return null
        return prosignsByLetters[text.substring(1, end)]
    }

    private fun prosignFor(token: String): MorseProsign? =
        if (MorseLetter.isValidCode(token)) prosignsByCode[MorseLetter(token)] else null

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

    /** The first code point of a non-empty string: two UTF-16 chars for a surrogate pair (e.g. an emoji). */
    private fun String.firstCodePoint(): String {
        val isPair = this[0].isHighSurrogate() && length > 1 && this[1].isLowSurrogate()
        return substring(0, if (isPair) 2 else 1)
    }

    companion object {
        /** Stands in for a Morse letter that couldn't be decoded. */
        const val REPLACEMENT_CHAR = '\uFFFD'
    }
}
