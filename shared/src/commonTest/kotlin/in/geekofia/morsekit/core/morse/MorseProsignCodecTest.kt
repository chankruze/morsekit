package `in`.geekofia.morsekit.core.morse

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MorseProsignCodecTest {
    private val codec = MorseCodec()

    @Test
    fun prosignCodesDecodeInAngleBrackets() {
        val result = codec.decode("...---...")
        assertEquals("<SOS>", result.text)
        assertTrue(result.isValid, "a prosign isn't an unknown code")
        assertEquals("<HH>", codec.decode("........").text)
        assertEquals("S<SOS> <CL>", codec.decode("... ...---... / -.-..-..").text)
    }

    @Test
    fun codesSharedWithPunctuationStayPunctuation() {
        assertEquals("+", codec.decode(".-.-.").text) // AR
        assertEquals("=", codec.decode("-...-").text) // BT
        assertEquals("(", codec.decode("-.--.").text) // KN
        assertEquals("&", codec.decode(".-...").text) // AS
    }

    @Test
    fun aProsignInBracketsEncodesAsOneLetter() {
        val result = codec.encode("<SOS>")
        assertEquals("...---...", result.morse)
        assertEquals(1, result.message.words.single().letters.size, "sent as one sign, no gaps")
        assertTrue(result.isValid)
        assertEquals("...---...", codec.encode("<sos>").morse, "case-insensitive like any text")
    }

    @Test
    fun anUnknownOrUnclosedBracketIsJustText() {
        val unknown = codec.encode("<XYZ>")
        assertEquals("-..- -.-- --..", unknown.morse)
        assertEquals(listOf("<", ">"), unknown.issues.map { (it as TranslationIssue.UnsupportedCharacter).character })
        val unclosed = codec.encode("<SOS")
        assertEquals("... --- ...", unclosed.morse)
        assertEquals(listOf("<"), unclosed.issues.map { (it as TranslationIssue.UnsupportedCharacter).character })
    }

    @Test
    fun prosignsSurviveARoundTrip() {
        val text = "SEND <SOS> NOW"
        assertEquals(text, codec.decode(codec.encode(text).morse).text)
    }
}
