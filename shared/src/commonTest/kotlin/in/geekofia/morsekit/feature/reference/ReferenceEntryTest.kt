package `in`.geekofia.morsekit.feature.reference

import `in`.geekofia.morsekit.core.morse.ExpectedMorse
import `in`.geekofia.morsekit.core.morse.MorseAlphabet
import `in`.geekofia.morsekit.core.morse.MorseCategory
import `in`.geekofia.morsekit.core.morse.MorseProsigns
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReferenceEntryTest {
    private val entries = MorseAlphabet.International.referenceEntries()

    @Test
    fun containsEveryAlphabetCharacterInOrder() {
        assertEquals(MorseAlphabet.International.mappings.map { it.character.toString() }, entries.map { it.symbol })
    }

    @Test
    fun codesMatchTheIndependentTable() {
        entries.forEach { assertEquals(ExpectedMorse.all.getValue(it.symbol.single()), it.code, "'${it.symbol}'") }
    }

    @Test
    fun coversLettersDigitsAndPunctuation() {
        val byCategory = entries.groupBy { it.category }
        assertEquals(('A'..'Z').toList(), byCategory.getValue(MorseCategory.Letter).map { it.symbol.single() })
        assertEquals(('0'..'9').toList(), byCategory.getValue(MorseCategory.Digit).map { it.symbol.single() })
        assertEquals(ExpectedMorse.punctuation.keys, byCategory.getValue(MorseCategory.Punctuation).map { it.symbol.single() }.toSet())
    }

    @Test
    fun everyPunctuationMarkHasAName() {
        entries.filter { it.category == MorseCategory.Punctuation }
            .forEach { assertNotNull(it.name, "'${it.symbol}' has no name") }
    }

    @Test
    fun namesExistOnlyForAlphabetPunctuation() {
        val punctuation = entries.filter { it.category == MorseCategory.Punctuation }.map { it.symbol.single() }.toSet()
        assertEquals(punctuation, punctuationNames.keys)
    }

    @Test
    fun lettersAndDigitsHaveNoName() {
        entries.filter { it.category != MorseCategory.Punctuation }.forEach { assertNull(it.name) }
    }

    @Test
    fun accessibilityLabelSpellsOutTheCode() {
        assertEquals("A, dot dash", entries.first { it.symbol == "A" }.accessibilityLabel)
        assertEquals("Comma, dash dash dot dot dash dash", entries.first { it.symbol == "," }.accessibilityLabel)
    }

    @Test
    fun prosignsBecomeEntriesWithTheirMeaning() {
        val prosigns = MorseProsigns.Common.referenceEntries()
        assertEquals(MorseProsigns.Common.map { it.letters }, prosigns.map { it.symbol })
        prosigns.forEach {
            assertEquals(MorseCategory.Prosign, it.category)
            assertTrue(it.isProsign)
            assertNotNull(it.name)
        }
        assertEquals("...---...", prosigns.first { it.symbol == "SOS" }.code)
    }

    @Test
    fun prosignLabelSpellsTheLettersOut() {
        val sos = MorseProsigns.Common.referenceEntries().first { it.symbol == "SOS" }
        assertEquals(
            "Prosign S O S, Distress signal, dot dot dot dash dash dash dot dot dot",
            sos.accessibilityLabel,
        )
    }

    @Test
    fun alphabetEntriesAreNotProsigns() {
        assertFalse(entries.any { it.isProsign })
    }
}
