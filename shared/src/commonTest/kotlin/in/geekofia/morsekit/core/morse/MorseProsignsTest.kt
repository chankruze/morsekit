package `in`.geekofia.morsekit.core.morse

import kotlin.test.Test
import kotlin.test.assertEquals

class MorseProsignsTest {
    /** Written out independently, like [ExpectedMorse]. */
    private val expected = mapOf(
        "SOS" to "...---...",
        "AR" to ".-.-.",
        "SK" to "...-.-",
        "BT" to "-...-",
        "KN" to "-.--.",
        "AS" to ".-...",
        "CT" to "-.-.-",
        "VE" to "...-.",
        "HH" to "........",
        "CL" to "-.-..-..",
    )

    @Test
    fun codesAreTheLettersRunTogether() {
        assertEquals(expected, MorseProsigns.Common.associate { it.letters to it.code.code })
    }

    @Test
    fun someShareACodeWithPunctuation() {
        val alphabet = MorseAlphabet.International
        val byLetters = MorseProsigns.Common.associateBy { it.letters }
        assertEquals('+', alphabet.characterFor(byLetters.getValue("AR").code))
        assertEquals('=', alphabet.characterFor(byLetters.getValue("BT").code))
        assertEquals('(', alphabet.characterFor(byLetters.getValue("KN").code))
        assertEquals('&', alphabet.characterFor(byLetters.getValue("AS").code))
    }

    @Test
    fun everyProsignHasAMeaningAndUniqueLetters() {
        MorseProsigns.Common.forEach { assertEquals(true, it.meaning.isNotBlank(), it.letters) }
        assertEquals(MorseProsigns.Common.size, MorseProsigns.Common.map { it.letters }.toSet().size)
    }
}
