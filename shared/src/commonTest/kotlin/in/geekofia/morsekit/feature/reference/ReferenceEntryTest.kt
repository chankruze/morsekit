package `in`.geekofia.morsekit.feature.reference

import `in`.geekofia.morsekit.core.morse.ExpectedMorse
import `in`.geekofia.morsekit.core.morse.MorseAlphabet
import `in`.geekofia.morsekit.core.morse.MorseCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ReferenceEntryTest {
    private val entries = MorseAlphabet.International.referenceEntries()

    @Test
    fun containsEveryAlphabetCharacterInOrder() {
        assertEquals(MorseAlphabet.International.mappings.map { it.character }, entries.map { it.character })
    }

    @Test
    fun codesMatchTheIndependentTable() {
        entries.forEach { assertEquals(ExpectedMorse.all.getValue(it.character), it.code, "'${it.character}'") }
    }

    @Test
    fun coversLettersDigitsAndPunctuation() {
        val byCategory = entries.groupBy { it.category }
        assertEquals(('A'..'Z').toList(), byCategory.getValue(MorseCategory.Letter).map { it.character })
        assertEquals(('0'..'9').toList(), byCategory.getValue(MorseCategory.Digit).map { it.character })
        assertEquals(ExpectedMorse.punctuation.keys, byCategory.getValue(MorseCategory.Punctuation).map { it.character }.toSet())
    }

    @Test
    fun everyPunctuationMarkHasAName() {
        entries.filter { it.category == MorseCategory.Punctuation }
            .forEach { assertNotNull(it.name, "'${it.character}' has no name") }
    }

    @Test
    fun namesExistOnlyForAlphabetPunctuation() {
        val punctuation = entries.filter { it.category == MorseCategory.Punctuation }.map { it.character }.toSet()
        assertEquals(punctuation, punctuationNames.keys)
    }

    @Test
    fun lettersAndDigitsHaveNoName() {
        entries.filter { it.category != MorseCategory.Punctuation }.forEach { assertNull(it.name) }
    }

    @Test
    fun accessibilityLabelSpellsOutTheCode() {
        assertEquals("A, dot dash", entries.first { it.character == 'A' }.accessibilityLabel)
        assertEquals("Comma, dash dash dot dot dash dash", entries.first { it.character == ',' }.accessibilityLabel)
    }
}
