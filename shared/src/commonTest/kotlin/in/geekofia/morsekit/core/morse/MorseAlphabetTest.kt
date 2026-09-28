package `in`.geekofia.morsekit.core.morse

import `in`.geekofia.morsekit.core.model.MorseLetter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MorseAlphabetTest {
    private val alphabet = MorseAlphabet.International

    @Test
    fun containsAllLettersAndDigits() {
        (('A'..'Z') + ('0'..'9')).forEach { assertTrue(it in alphabet, "Missing '$it'") }
    }

    @Test
    fun containsExpectedPunctuation() {
        ".,?'!/()&:;=+-_\"$@".forEach { assertTrue(it in alphabet, "Missing '$it'") }
    }

    @Test
    fun storesOnlyUpperCaseLetters() {
        ('a'..'z').forEach { assertFalse(it in alphabet, "Unexpected lower-case '$it'") }
    }

    @Test
    fun codesAreUnique() {
        val codes = alphabet.mappings.map { it.code }
        assertEquals(codes.size, codes.toSet().size)
    }

    @Test
    fun lookupsAreInverse() {
        alphabet.mappings.forEach { (char, code) ->
            assertEquals(code, alphabet.codeFor(char))
            assertEquals(char, alphabet.characterFor(code))
        }
    }

    @Test
    fun unknownLookupsReturnNull() {
        assertNull(alphabet.codeFor('#'))
        assertNull(alphabet.characterFor(MorseLetter("........")))
    }

    @Test
    fun categorizesMappings() {
        val byCategory = alphabet.mappings.groupBy { it.category }
        assertEquals(26, byCategory.getValue(MorseCategory.Letter).size)
        assertEquals(10, byCategory.getValue(MorseCategory.Digit).size)
        assertEquals(18, byCategory.getValue(MorseCategory.Punctuation).size)
    }

    @Test
    fun rejectsDuplicateCodes() {
        assertFailsWith<IllegalArgumentException> {
            MorseAlphabet(listOf(MorseMapping('A', MorseLetter(".-")), MorseMapping('B', MorseLetter(".-"))))
        }
    }

    @Test
    fun rejectsDuplicateCharacters() {
        assertFailsWith<IllegalArgumentException> {
            MorseAlphabet(listOf(MorseMapping('A', MorseLetter(".-")), MorseMapping('A', MorseLetter("-..."))))
        }
    }

    @Test
    fun morseLetterRejectsInvalidCodes() {
        assertFailsWith<IllegalArgumentException> { MorseLetter("") }
        assertFailsWith<IllegalArgumentException> { MorseLetter(".x-") }
        assertFailsWith<IllegalArgumentException> { MorseLetter(". -") }
    }
}
