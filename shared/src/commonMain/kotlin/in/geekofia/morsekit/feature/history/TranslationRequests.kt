package `in`.geekofia.morsekit.feature.history

import `in`.geekofia.morsekit.core.history.HistoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A history entry to open in the translator. History and the translator are separate screens with
 * their own view models, so History leaves the entry here (app-scoped, in `AppContainer`) and the
 * translator picks it up and [consume]s it.
 */
class TranslationRequests {
    private val state = MutableStateFlow<HistoryEntry?>(null)

    val pending: StateFlow<HistoryEntry?> = state.asStateFlow()

    fun open(entry: HistoryEntry) {
        state.value = entry
    }

    fun consume() {
        state.value = null
    }
}
