package `in`.geekofia.morsekit.feature.translator

import `in`.geekofia.morsekit.core.model.TranslationDirection
import `in`.geekofia.morsekit.core.morse.MorseCodec
import `in`.geekofia.morsekit.core.morse.TranslationIssue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TranslatorUiStateTest {
    private val codec = MorseCodec()

    private fun textToMorse(input: String) = codec.encode(input).let {
        TranslatorUiState(TranslationDirection.TextToMorse, input, it.morse, it.issues)
    }

    private fun morseToText(input: String) = codec.decode(input).let {
        TranslatorUiState(TranslationDirection.MorseToText, input, it.text, it.issues)
    }

    @Test
    fun blankInputIsEmpty() {
        assertEquals(TranslationStatus.Empty, TranslatorUiState().status)
        assertEquals(TranslationStatus.Empty, textToMorse("   ").status)
        assertEquals(TranslationStatus.Empty, morseToText(" / ").status)
    }

    @Test
    fun fullyTranslatedInputIsComplete() {
        assertEquals(TranslationStatus.Complete, textToMorse("SOS").status)
        assertEquals(TranslationStatus.Complete, morseToText("... --- ...").status)
    }

    @Test
    fun inputWithSomeIssuesIsPartial() {
        assertEquals(TranslationStatus.Partial, textToMorse("SOS #").status)
        assertEquals(TranslationStatus.Partial, morseToText("... -x-").status)
    }

    @Test
    fun inputWithNothingTranslatableIsInvalid() {
        assertEquals(TranslationStatus.Invalid, textToMorse("### 😀").status)
        assertEquals(TranslationStatus.Invalid, morseToText("hello").status)
        assertEquals(TranslationStatus.Invalid, morseToText("......... -x-").status)
    }

    @Test
    fun hasOutputIgnoresPlaceholdersAndWhitespace() {
        assertTrue(textToMorse("E").hasOutput)
        assertFalse(TranslatorUiState(output = "${MorseCodec.REPLACEMENT_CHAR} ${MorseCodec.REPLACEMENT_CHAR}").hasOutput)
        assertFalse(TranslatorUiState(output = "   ").hasOutput)
    }

    @Test
    fun issuesAreGroupedByKind() {
        val issues = listOf(
            TranslationIssue.UnsupportedCharacter("#"),
            TranslationIssue.UnknownCode("........"),
            TranslationIssue.UnsupportedCharacter("😀"),
            TranslationIssue.MalformedCode("-x-"),
        )
        assertEquals(
            listOf(
                "No Morse code for “#”, “😀” (skipped)",
                "Unknown Morse code: “........”",
                "Use only dots and dashes: “-x-”",
            ),
            issueMessages(issues),
        )
    }

    @Test
    fun longIssueListsAreTruncated() {
        val issues = "ÀÁÂÃÄÅÆ".map { TranslationIssue.UnsupportedCharacter(it.toString()) }
        assertEquals(listOf("No Morse code for “À”, “Á”, “Â”, “Ã”, “Ä” and 2 more (skipped)"), issueMessages(issues))
    }

    @Test
    fun invisibleCharactersAreShownAsCodePoints() {
        val issues = listOf(TranslationIssue.UnsupportedCharacter("\uFE0F"))
        assertEquals(listOf("No Morse code for “U+FE0F” (skipped)"), issueMessages(issues))
    }

    @Test
    fun noIssuesMeansNoMessages() {
        assertEquals(emptyList(), issueMessages(emptyList()))
    }
}
