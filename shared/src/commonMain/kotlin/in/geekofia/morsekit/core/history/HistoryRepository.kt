package `in`.geekofia.morsekit.core.history

import `in`.geekofia.morsekit.core.model.TranslationDirection
import `in`.geekofia.morsekit.platform.KeyValueStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet

/**
 * Translations that were used, newest first. Kept in its own [store] (not the settings store)
 * so each platform can keep it out of device backups.
 *
 * - Using the same text again (same direction, same trimmed input) moves it to the top instead
 *   of adding a copy; it stays a favourite if it was one.
 * - At most [MAX_RECENT] non-favourites are kept; favourites are never dropped for space.
 */
class HistoryRepository(
    private val store: KeyValueStore,
    private val nowMillis: () -> Long,
) {
    private val state = MutableStateFlow(HistoryCodec.decode(store.getString(KEY_ENTRIES)))

    val entries: StateFlow<List<HistoryEntry>> = state.asStateFlow()

    fun record(direction: TranslationDirection, input: String, output: String) {
        if (input.isBlank() || output.isBlank()) return
        update { entries ->
            val existing = entries.firstOrNull { it.direction == direction && it.input.trim() == input.trim() }
            val entry = HistoryEntry(
                id = existing?.id ?: ((entries.maxOfOrNull { it.id } ?: 0) + 1),
                direction = direction,
                input = input,
                output = output,
                savedAt = nowMillis(),
                favorite = existing?.favorite ?: false,
            )
            trim(listOf(entry) + entries.filter { it.id != entry.id })
        }
    }

    fun setFavorite(id: Long, favorite: Boolean) = update { entries ->
        trim(entries.map { if (it.id == id) it.copy(favorite = favorite) else it })
    }

    fun delete(id: Long) = update { entries -> entries.filter { it.id != id } }

    /** Puts a deleted [entry] back where it was (Undo): by its time, newest first. */
    fun restore(entry: HistoryEntry) = update { entries ->
        trim((entries.filter { it.id != entry.id } + entry).sortedByDescending { it.savedAt })
    }

    /** Removes everything except favourites. */
    fun clearRecent() = update { entries -> entries.filter { it.favorite } }

    private fun trim(entries: List<HistoryEntry>): List<HistoryEntry> {
        var recent = 0
        return entries.filter { it.favorite || ++recent <= MAX_RECENT }
    }

    private fun update(transform: (List<HistoryEntry>) -> List<HistoryEntry>) {
        store.putString(KEY_ENTRIES, HistoryCodec.encode(state.updateAndGet(transform)))
    }

    internal companion object {
        const val KEY_ENTRIES = "history.entries"
        const val MAX_RECENT = 50
    }
}
