package `in`.geekofia.morsekit.core.morse

import kotlin.test.Test
import kotlin.test.assertEquals

class MorseNormalizerTest {

    @Test
    fun normalizeTextUpperCasesAndCollapsesWhitespace() {
        assertEquals("HELLO WORLD", MorseNormalizer.normalizeText("  hello \t\n  world "))
        assertEquals("", MorseNormalizer.normalizeText("   "))
    }

    @Test
    fun normalizeTextKeepsUnsupportedCharacters() {
        assertEquals("CAFÉ #1", MorseNormalizer.normalizeText("cafÉ #1"))
    }

    @Test
    fun normalizeTextCharOnlyUpperCasesAscii() {
        assertEquals('A', MorseNormalizer.normalizeTextChar('a'))
        assertEquals('é', MorseNormalizer.normalizeTextChar('é'))
        assertEquals('ı', MorseNormalizer.normalizeTextChar('ı'))
    }

    @Test
    fun normalizeTextCharFoldsSmartQuotes() {
        "‘’‚′".forEach { assertEquals('\'', MorseNormalizer.normalizeTextChar(it)) }
        "“”„″".forEach { assertEquals('"', MorseNormalizer.normalizeTextChar(it)) }
    }

    @Test
    fun normalizeMorseProducesCanonicalNotation() {
        assertEquals("... / --- / ...", MorseNormalizer.normalizeMorse("•••   ———|···"))
        assertEquals("... --- ...", MorseNormalizer.normalizeMorse("  ...\t--- ...  "))
    }

    @Test
    fun normalizeMorseKeepsInvalidTokens() {
        assertEquals("... abc / ---", MorseNormalizer.normalizeMorse("... abc/---"))
    }

    @Test
    fun normalizeMorseCharFoldsAlternatives() {
        "·•∙⋅".forEach { assertEquals('.', MorseNormalizer.normalizeMorseChar(it)) }
        "−–—‒_".forEach { assertEquals('-', MorseNormalizer.normalizeMorseChar(it)) }
        assertEquals('x', MorseNormalizer.normalizeMorseChar('x'))
    }
}
