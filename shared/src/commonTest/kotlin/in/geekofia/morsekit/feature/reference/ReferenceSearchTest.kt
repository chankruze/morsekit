package `in`.geekofia.morsekit.feature.reference

import `in`.geekofia.morsekit.core.morse.MorseAlphabet
import `in`.geekofia.morsekit.core.morse.MorseProsigns
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReferenceSearchTest {
    private val entries = MorseAlphabet.International.referenceEntries()

    private fun search(query: String) = filterReference(entries, query).map { it.symbol }

    @Test
    fun blankQueryReturnsEverything() {
        assertEquals(entries, filterReference(entries, ""))
        assertEquals(entries, filterReference(entries, "   "))
    }

    @Test
    fun singleCharacterMatchesCaseInsensitively() {
        assertEquals(listOf("A"), search("a"))
        assertEquals(listOf("A"), search("A"))
        assertEquals(listOf("7"), search("7"))
        assertEquals(listOf("?"), search("?"))
    }

    @Test
    fun surroundingWhitespaceIsIgnored() {
        assertEquals(listOf("Q"), search("  q "))
    }

    @Test
    fun codeQueryMatchesByPrefix() {
        // H ...., S ..., V ...-, 3 ...--, 4 ....-, 5 ....., $ ...-..-  (alphabet order)
        assertEquals(listOf("H", "S", "V", "3", "4", "5", "$"), search("..."))
        val dashLetters = search("-").filter { it.single().isLetter() }
        assertTrue(listOf("T", "N", "M").all { it in dashLetters })
        assertFalse("E" in dashLetters)
    }

    @Test
    fun exactCodeIsIncludedInPrefixMatches() {
        assertTrue("A" in search(".-"))
        assertTrue(search(".-").all { entries.first { e -> e.symbol == it }.code.startsWith(".-") })
    }

    @Test
    fun typographicDotsAndDashesWork() {
        assertEquals(search(".-"), search("·−"))
        assertEquals(search("..."), search("•••"))
    }

    @Test
    fun ambiguousSymbolMatchesBothCharacterAndCode() {
        // "." is the period character and also the code prefix "dot".
        val results = search(".")
        assertTrue("." in results)
        assertTrue("E" in results)
        // "-" is the hyphen character and also the code prefix "dash".
        assertTrue("-" in search("-"))
        assertTrue("T" in search("-"))
    }

    @Test
    fun namesMatchPartiallyAndCaseInsensitively() {
        assertEquals(listOf(","), search("comma"))
        assertEquals(listOf("?"), search("QUEST"))
        assertEquals(listOf("."), search("full stop"))
        assertEquals(listOf("(", ")"), search("parenthesis"))
    }

    @Test
    fun singleLetterDoesNotMatchNames() {
        // "c" should find C, not Comma/Colon/Close parenthesis.
        assertEquals(listOf("C"), search("c"))
    }

    @Test
    fun resultsKeepAlphabetOrder() {
        val results = search("-")
        val order = entries.map { it.symbol }
        assertEquals(results.sortedBy(order::indexOf), results)
    }

    @Test
    fun unknownQueryReturnsNothing() {
        assertEquals(emptyList(), search("xyz"))
        assertEquals(emptyList(), search("é"))
        assertEquals(emptyList(), search("........"))
    }

    private val withProsigns = entries + MorseProsigns.Common.referenceEntries()

    private fun searchAll(query: String) = filterReference(withProsigns, query).map { it.symbol }

    @Test
    fun prosignsMatchByLettersCaseInsensitively() {
        assertTrue("SOS" in searchAll("sos"))
        assertTrue("AR" in searchAll("AR"))
    }

    @Test
    fun prosignsMatchByMeaning() {
        assertEquals(listOf("SOS"), searchAll("distress"))
        assertEquals(listOf("AR", "SK"), searchAll("end of"))
    }

    @Test
    fun prosignsMatchByCodePrefix() {
        // HH is eight dots: not a character, but a prosign.
        assertEquals(listOf("HH"), searchAll("........"))
        assertTrue("SOS" in searchAll("...---"))
    }

    @Test
    fun singleLetterDoesNotMatchProsigns() {
        assertEquals(listOf("S"), searchAll("s"))
    }
}
