package `in`.geekofia.morsekit.core.morse

import `in`.geekofia.morsekit.core.morse.TranslationIssue.UnsupportedCharacter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MorseEncodeTest {
    private val codec = MorseCodec()

    private fun encode(text: String) = codec.encode(text).morse

    @Test
    fun encodesSos() {
        assertEquals("... --- ...", encode("SOS"))
    }

    @Test
    fun encodesEveryUpperCaseLetter() {
        ExpectedMorse.letters.forEach { (char, code) -> assertEquals(code, encode(char.toString()), "'$char'") }
    }

    @Test
    fun encodesEveryLowerCaseLetterLikeUpperCase() {
        ExpectedMorse.letters.forEach { (char, code) ->
            assertEquals(code, encode(char.lowercase()), "'${char.lowercase()}'")
        }
    }

    @Test
    fun encodesFullAlphabetAsOneWord() {
        val expected = ('A'..'Z').joinToString(" ") { ExpectedMorse.letters.getValue(it) }
        assertEquals(expected, encode("abcdefghijklmnopqrstuvwxyz"))
    }

    @Test
    fun encodesDigits() {
        ExpectedMorse.digits.forEach { (char, code) -> assertEquals(code, encode(char.toString()), "'$char'") }
        assertEquals(".---- ..--- ...--", encode("123"))
    }

    @Test
    fun encodesPunctuation() {
        ExpectedMorse.punctuation.forEach { (char, code) ->
            assertEquals(code, encode(char.toString()), "'$char'")
        }
    }

    @Test
    fun encodesMixedSentence() {
        assertEquals(
            ".... . .-.. .-.. --- --..-- / .-- --- .-. .-.. -.. -.-.--",
            encode("Hello, World!"),
        )
    }

    @Test
    fun separatesWordsWithSlash() {
        assertEquals("... --- ... / ... --- ...", encode("SOS SOS"))
        assertEquals(".- / -... / -.-.", encode("a b c"))
    }

    @Test
    fun collapsesRepeatedAndSurroundingWhitespace() {
        assertEquals("... --- ... / ... --- ...", encode("   SOS  \t\n  SOS   "))
    }

    @Test
    fun treatsTabsNewlinesAndNonBreakingSpacesAsWordBreaks() {
        assertEquals(".- / -...", encode("A\tB"))
        assertEquals(".- / -...", encode("A\nB"))
        assertEquals(".- / -...", encode("A\u00A0B"))
    }

    @Test
    fun emptyInputProducesEmptyValidResult() {
        val result = codec.encode("")
        assertEquals("", result.morse)
        assertTrue(result.message.isEmpty)
        assertTrue(result.isValid)
    }

    @Test
    fun whitespaceOnlyInputProducesEmptyResult() {
        assertEquals("", encode("   \n\t "))
    }

    @Test
    fun skipsAndReportsUnsupportedCharacters() {
        val result = codec.encode("S#O%S")
        assertEquals("... --- ...", result.morse)
        assertFalse(result.isValid)
        assertEquals(listOf(UnsupportedCharacter("#"), UnsupportedCharacter("%")), result.issues)
    }

    @Test
    fun reportsEachUnsupportedCharacterOnce() {
        val result = codec.encode("é é é")
        assertEquals(listOf(UnsupportedCharacter("é")), result.issues)
    }

    @Test
    fun dropsWordsMadeOnlyOfUnsupportedCharacters() {
        assertEquals("... / ---", encode("S ### O"))
    }

    @Test
    fun doesNotFoldNonAsciiLettersIntoAscii() {
        // 'ı'.uppercaseChar() == 'I'; it must not be silently encoded as I.
        assertEquals(listOf(UnsupportedCharacter("ı")), codec.encode("ı").issues)
    }

    @Test
    fun reportsEmojiAsOneCharacterNotSurrogateHalves() {
        val result = codec.encode("Hi 😀😀")
        assertEquals(".... ..", result.morse)
        assertEquals(listOf(UnsupportedCharacter("😀")), result.issues)
    }

    @Test
    fun loneSurrogateIsReportedWithoutCrashing() {
        assertEquals(listOf(UnsupportedCharacter("\uD83D")), codec.encode("A\uD83D").issues)
    }

    @Test
    fun normalizesSmartQuotes() {
        assertEquals(encode("It's \"ok\""), encode("It’s “ok”"))
        assertTrue(codec.isEncodable("It’s “ok”"))
    }

    @Test
    fun encodeCharHandlesCaseAndUnsupported() {
        assertEquals("...", codec.encodeChar('s'))
        assertEquals("...", codec.encodeChar('S'))
        assertEquals("-----", codec.encodeChar('0'))
        assertNull(codec.encodeChar('#'))
        assertNull(codec.encodeChar(' '))
    }

    @Test
    fun isEncodableReflectsIssues() {
        assertTrue(codec.isEncodable("Hello World 123"))
        assertFalse(codec.isEncodable("Hello #"))
    }

    @Test
    fun messageStructureMatchesOutput() {
        val message = codec.encode("HI YOU").message
        assertEquals(2, message.words.size)
        assertEquals(listOf("....", ".."), message.words[0].letters.map { it.code })
        assertEquals(message.toString(), codec.encode("HI YOU").morse)
    }
}
