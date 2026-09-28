package `in`.geekofia.morsekit.feature.translator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import `in`.geekofia.morsekit.core.model.TranslationDirection
import `in`.geekofia.morsekit.core.morse.MorseCodec

/**
 * Holds translator state and delegates all translation to [MorseCodec].
 *
 * State is Compose snapshot state rather than a StateFlow because it backs a TextField: updates
 * must be synchronous or the cursor/IME can get out of sync while typing.
 */
class TranslatorViewModel(
    private val codec: MorseCodec = MorseCodec(),
) : ViewModel() {

    var uiState by mutableStateOf(TranslatorUiState())
        private set

    fun onInputChange(input: String) {
        uiState = translate(uiState.direction, input)
    }

    /** Selecting the other direction swaps: the current output becomes the new input. */
    fun onDirectionSelected(direction: TranslationDirection) {
        if (direction != uiState.direction) swapDirection()
    }

    fun swapDirection() {
        uiState = translate(uiState.direction.reversed, uiState.output)
    }

    fun onClear() {
        uiState = TranslatorUiState(direction = uiState.direction)
    }

    private fun translate(direction: TranslationDirection, input: String): TranslatorUiState =
        when (direction) {
            TranslationDirection.TextToMorse -> codec.encode(input).let {
                TranslatorUiState(direction, input, output = it.morse, issues = it.issues)
            }
            TranslationDirection.MorseToText -> codec.decode(input).let {
                TranslatorUiState(direction, input, output = it.text, issues = it.issues)
            }
        }
}
