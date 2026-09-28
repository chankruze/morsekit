package `in`.geekofia.morsekit.feature.reference

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import `in`.geekofia.morsekit.core.morse.MorseAlphabet

/**
 * Holds the reference chart and the search query. Like the translator, state is Compose snapshot
 * state so the search field updates synchronously.
 */
class ReferenceViewModel(
    alphabet: MorseAlphabet = MorseAlphabet.International,
) : ViewModel() {

    private val entries = alphabet.referenceEntries()

    var uiState by mutableStateOf(ReferenceUiState(sections = entries.toSections()))
        private set

    fun onQueryChange(query: String) {
        uiState = ReferenceUiState(query = query, sections = filterReference(entries, query).toSections())
    }
}
