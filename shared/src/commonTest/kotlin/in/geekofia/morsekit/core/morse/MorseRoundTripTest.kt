package `in`.geekofia.morsekit.core.morse

import kotlin.test.Test
import kotlin.test.assertEquals

class MorseRoundTripTest {
    private val codec = MorseCodec()

    private fun roundTrip(text: String) = codec.decode(codec.encode(text).morse).text

    @Test
    fun everySupportedCharacterRoundTrips() {
        MorseAlphabet.International.characters.forEach {
            assertEquals(it.toString(), roundTrip(it.toString()), "'$it'")
        }
    }

    @Test
    fun allSupportedCharactersInOneWordRoundTrip() {
        val word = MorseAlphabet.International.characters.joinToString("")
        assertEquals(word, roundTrip(word))
    }

    @Test
    fun sentencesRoundTripToNormalizedText() {
        listOf(
            "SOS",
            "THE QUICK BROWN FOX JUMPS OVER THE LAZY DOG",
            "HELLO, WORLD!",
            "MEET @ 10:30 (ROOM 4-B); BRING $5 & A \"PEN\"?",
            "A/B = C + D_E. 'OK'",
        ).forEach { assertEquals(it, roundTrip(it)) }
    }

    @Test
    fun roundTripUpperCasesAndCollapsesWhitespace() {
        assertEquals("HELLO WORLD", roundTrip("  hello \n\t world  "))
    }

    @Test
    fun roundTripDropsUnsupportedCharacters() {
        assertEquals("SOS", roundTrip("S#O#S"))
    }

    @Test
    fun morseRoundTripsToCanonicalNotation() {
        val canonical = "... --- ... / .... ."
        listOf(canonical, "··· −−− ··· | ···· ·", " ... --- ...\n.... . ").forEach {
            assertEquals(canonical, codec.encode(codec.decode(it).text).morse, it)
        }
    }
}
