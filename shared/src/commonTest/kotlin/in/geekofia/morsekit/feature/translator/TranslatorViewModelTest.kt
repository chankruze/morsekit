package `in`.geekofia.morsekit.feature.translator

import `in`.geekofia.morsekit.core.model.TranslationDirection
import `in`.geekofia.morsekit.core.morse.MorseCodec
import `in`.geekofia.morsekit.core.morse.TranslationIssue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TranslatorViewModelTest {
    private val viewModel = TranslatorViewModel()

    @Test
    fun startsEmptyInTextToMorse() {
        assertEquals(TranslatorUiState(), viewModel.uiState)
    }

    @Test
    fun translatesTextToMorseOnInput() {
        viewModel.onInputChange("sos")
        assertEquals("sos", viewModel.uiState.input)
        assertEquals("... --- ...", viewModel.uiState.output)
        assertTrue(viewModel.uiState.issues.isEmpty())
    }

    @Test
    fun surfacesIssues() {
        viewModel.onInputChange("S#")
        assertEquals(listOf(TranslationIssue.UnsupportedCharacter("#")), viewModel.uiState.issues)
    }

    @Test
    fun swapMovesOutputIntoInput() {
        viewModel.onInputChange("SOS")
        viewModel.swapDirection()
        with(viewModel.uiState) {
            assertEquals(TranslationDirection.MorseToText, direction)
            assertEquals("... --- ...", input)
            assertEquals("SOS", output)
        }
    }

    @Test
    fun selectingOtherDirectionSwaps() {
        viewModel.onInputChange("SOS")
        viewModel.onDirectionSelected(TranslationDirection.MorseToText)
        assertEquals("SOS", viewModel.uiState.output)
    }

    @Test
    fun selectingCurrentDirectionDoesNothing() {
        viewModel.onInputChange("SOS")
        val before = viewModel.uiState
        viewModel.onDirectionSelected(TranslationDirection.TextToMorse)
        assertEquals(before, viewModel.uiState)
    }

    @Test
    fun translatesMorseToTextOnInput() {
        viewModel.onDirectionSelected(TranslationDirection.MorseToText)
        viewModel.onInputChange(".... .. / - .... . .-. .")
        assertEquals("HI THERE", viewModel.uiState.output)
    }

    @Test
    fun clearKeepsDirection() {
        viewModel.onDirectionSelected(TranslationDirection.MorseToText)
        viewModel.onInputChange("...")
        viewModel.onClear()
        assertEquals(TranslatorUiState(direction = TranslationDirection.MorseToText), viewModel.uiState)
    }

    @Test
    fun swapDropsPlaceholdersForUnreadableMorse() {
        viewModel.onDirectionSelected(TranslationDirection.MorseToText)
        viewModel.onInputChange("... -x- ...")
        assertEquals("S${MorseCodec.REPLACEMENT_CHAR}S", viewModel.uiState.output)

        viewModel.swapDirection()
        with(viewModel.uiState) {
            assertEquals(TranslationDirection.TextToMorse, direction)
            assertEquals("SS", input)
            assertTrue(issues.isEmpty())
        }
    }

    @Test
    fun swapWithEmptyOutputJustChangesDirection() {
        viewModel.swapDirection()
        assertEquals(TranslatorUiState(direction = TranslationDirection.MorseToText), viewModel.uiState)
    }

    @Test
    fun liveConversionUpdatesOnEveryChange() {
        listOf("S" to "...", "SO" to "... ---", "SOS" to "... --- ...").forEach { (input, morse) ->
            viewModel.onInputChange(input)
            assertEquals(morse, viewModel.uiState.output)
        }
    }
}
