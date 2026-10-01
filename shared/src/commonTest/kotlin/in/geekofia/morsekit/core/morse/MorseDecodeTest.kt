package `in`.geekofia.morsekit.core.morse

import `in`.geekofia.morsekit.core.morse.MorseCodec.Companion.REPLACEMENT_CHAR
import `in`.geekofia.morsekit.core.morse.TranslationIssue.MalformedCode
import `in`.geekofia.morsekit.core.morse.TranslationIssue.UnknownCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MorseDecodeTest {
    private val codec = MorseCodec()

    private fun decode(morse: String) = codec.decode(morse).text

    @Test
    fun decodesSos() {
        assertEquals("SOS", decode("... --- ..."))
    }

    @Test
    fun decodesEveryCharacter() {
        ExpectedMorse.all.forEach { (char, code) -> assertEquals(char.toString(), decode(code), code) }
    }

    @Test
    fun decodesDigits() {
        assertEquals("2026", decode("..--- ----- ..--- -...."))
    }

    @Test
    fun decodesPunctuationInContext() {
        assertEquals("HELLO, WORLD!", decode(".... . .-.. .-.. --- --..-- / .-- --- .-. .-.. -.. -.-.--"))
    }

    @Test
    fun decodesMultipleWordsWithSlash() {
        assertEquals("SOS SOS", decode("... --- ... / ... --- ..."))
        assertEquals("SOS SOS", decode("... --- .../... --- ..."))
    }

    @Test
    fun acceptsAlternativeWordSeparators() {
        assertEquals("SOS SOS", decode("... --- ... | ... --- ..."))
        assertEquals("SOS SOS", decode("... --- ...\n... --- ..."))
        assertEquals("SOS SOS", decode("... --- ...\r\n... --- ..."))
        assertEquals("SOS SOS", decode("... --- ...  ... --- ..."))
        assertEquals("SOS SOS", decode("... --- ...       ... --- ..."))
    }

    @Test
    fun singleWhitespaceSeparatesLetters() {
        assertEquals("SOS", decode("...\t---\t..."))
    }

    @Test
    fun ignoresSurroundingWhitespaceAndSeparators() {
        assertEquals("SOS", decode("   ... --- ...   "))
        assertEquals("SOS", decode(" / ... --- ... / "))
    }

    @Test
    fun collapsesEmptyWords() {
        assertEquals("E T", decode(". / / / -"))
        assertEquals("E T", decode(". //// -"))
    }

    @Test
    fun emptyAndBlankInputDecodeToEmpty() {
        assertEquals("", decode(""))
        assertEquals("", decode("   "))
        assertEquals("", decode(" / / "))
        assertTrue(codec.decode("").isValid)
    }

    @Test
    fun acceptsTypographicDotsAndDashes() {
        assertEquals("SOS", decode("··· −−− ···"))
        assertEquals("SOS", decode("••• ––– •••"))
        assertEquals("SOS", decode("∙∙∙ ——— ⋅⋅⋅"))
        assertEquals("SOS", decode("... ___ ..."))
    }

    @Test
    fun reportsUnknownCodes() {
        // Nine dots: no character and no prosign (eight dots is the prosign HH).
        val result = codec.decode("... ......... ...")
        assertEquals("S${REPLACEMENT_CHAR}S", result.text)
        assertEquals(listOf(UnknownCode(".........")), result.issues)
        assertFalse(result.isValid)
    }

    @Test
    fun reportsMalformedTokens() {
        val result = codec.decode("... -x- abc")
        assertEquals("S$REPLACEMENT_CHAR$REPLACEMENT_CHAR", result.text)
        assertEquals(listOf(MalformedCode("-x-"), MalformedCode("abc")), result.issues)
    }

    @Test
    fun reportsEachBadTokenOnce() {
        val result = codec.decode("?? ?? ??")
        assertEquals(listOf(MalformedCode("??")), result.issues)
        assertEquals("$REPLACEMENT_CHAR$REPLACEMENT_CHAR$REPLACEMENT_CHAR", result.text)
    }

    @Test
    fun plainTextIsMalformedMorse() {
        assertFalse(codec.isValidMorse("SOS"))
    }

    @Test
    fun decodeSymbolHandlesSingleCodes() {
        assertEquals('S', codec.decodeSymbol("..."))
        assertEquals('S', codec.decodeSymbol("  ···  "))
        assertEquals('0', codec.decodeSymbol("-----"))
        assertNull(codec.decodeSymbol(""))
        assertNull(codec.decodeSymbol("........"))
        assertNull(codec.decodeSymbol("... ---"))
    }

    @Test
    fun parseKeepsWellFormedLettersForPlayback() {
        val message = codec.parse("... ........ -x- / ---")
        assertEquals(listOf(listOf("...", "........"), listOf("---")), message.words.map { w -> w.letters.map { it.code } })
        assertEquals("... ........ / ---", message.toString())
    }

    @Test
    fun parseDropsWordsWithNothingPlayable() {
        assertEquals("---", codec.parse("abc / ---").toString())
        assertTrue(codec.parse("").isEmpty)
        assertTrue(codec.parse("hello").isEmpty)
    }

    @Test
    fun parseAcceptsTypographicDotsAndDashes() {
        assertEquals("... --- ...", codec.parse("··· −−− ···").toString())
    }

    @Test
    fun isValidMorseReflectsIssues() {
        assertTrue(codec.isValidMorse("... --- ... / .... .."))
        assertFalse(codec.isValidMorse("... ---x"))
    }
}
