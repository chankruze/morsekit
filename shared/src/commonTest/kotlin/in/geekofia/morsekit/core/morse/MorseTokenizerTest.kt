package `in`.geekofia.morsekit.core.morse

import kotlin.test.Test
import kotlin.test.assertEquals

class MorseTokenizerTest {

    @Test
    fun textWordsSplitsOnAnyWhitespaceRun() {
        assertEquals(listOf("A", "B", "C"), MorseTokenizer.textWords(" A  B\t\nC "))
        assertEquals(emptyList(), MorseTokenizer.textWords(""))
    }

    @Test
    fun textWordsKeepsSlashAsCharacter() {
        assertEquals(listOf("A/B"), MorseTokenizer.textWords("A/B"))
    }

    @Test
    fun morseWordsSplitsLettersOnSingleWhitespace() {
        assertEquals(listOf(listOf("...", "---", "...")), MorseTokenizer.morseWords("... --- ..."))
    }

    @Test
    fun morseWordsSplitsWordsOnSeparators() {
        val expected = listOf(listOf(".", "-"), listOf("..", "--"))
        listOf(". - / .. --", ". -/.. --", ". - | .. --", ". -\n.. --", ". -  .. --").forEach {
            assertEquals(expected, MorseTokenizer.morseWords(it), it)
        }
    }

    @Test
    fun morseWordsDropsEmptyWords() {
        assertEquals(listOf(listOf("."), listOf("-")), MorseTokenizer.morseWords("/ . / / - /"))
        assertEquals(emptyList(), MorseTokenizer.morseWords(" /  | "))
    }

    @Test
    fun morseWordsDoesNotValidateTokens() {
        assertEquals(listOf(listOf("abc", "?")), MorseTokenizer.morseWords("abc ?"))
    }
}
