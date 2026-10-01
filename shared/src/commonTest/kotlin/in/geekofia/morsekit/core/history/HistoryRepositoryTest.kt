package `in`.geekofia.morsekit.core.history

import `in`.geekofia.morsekit.core.model.TranslationDirection.MorseToText
import `in`.geekofia.morsekit.core.model.TranslationDirection.TextToMorse
import `in`.geekofia.morsekit.platform.InMemoryKeyValueStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HistoryRepositoryTest {
    private val store = InMemoryKeyValueStore()
    private var now = 1_000L
    private fun repository() = HistoryRepository(store, nowMillis = { now++ })

    @Test
    fun startsEmptyAndRecordsNewestFirst() {
        val repo = repository()
        assertEquals(emptyList(), repo.entries.value)
        repo.record(TextToMorse, "SOS", "... --- ...")
        repo.record(MorseToText, ".- -...", "AB")
        assertEquals(listOf("AB", "... --- ..."), repo.entries.value.map { it.output })
    }

    @Test
    fun blankTranslationsAreNotSaved() {
        val repo = repository()
        repo.record(TextToMorse, "  ", "")
        repo.record(TextToMorse, "#", "")
        assertEquals(emptyList(), repo.entries.value)
    }

    @Test
    fun usingTheSameTextAgainMovesItToTheTopAndKeepsItsFavourite() {
        val repo = repository()
        repo.record(TextToMorse, "SOS", "... --- ...")
        val sos = repo.entries.value.single()
        repo.setFavorite(sos.id, true)
        repo.record(TextToMorse, "HI", ".... ..")
        repo.record(TextToMorse, " SOS ", "... --- ...")
        val entries = repo.entries.value
        assertEquals(2, entries.size)
        assertEquals(sos.id, entries.first().id)
        assertTrue(entries.first().favorite)
        assertTrue(entries.first().savedAt > sos.savedAt)
    }

    @Test
    fun theSameTextInTheOtherDirectionIsSeparate() {
        val repo = repository()
        repo.record(TextToMorse, "E", ".")
        repo.record(MorseToText, "E", "?")
        assertEquals(2, repo.entries.value.size)
    }

    @Test
    fun onlyTheNewestFiftyNonFavouritesAreKept() {
        val repo = repository()
        repo.record(TextToMorse, "KEEP", "-.- . . .--.")
        repo.setFavorite(repo.entries.value.single().id, true)
        repeat(60) { repo.record(TextToMorse, "M$it", "--") }
        val entries = repo.entries.value
        assertEquals(HistoryRepository.MAX_RECENT, entries.count { !it.favorite })
        assertTrue(entries.any { it.input == "KEEP" && it.favorite })
        assertEquals("M59", entries.first().input)
        assertTrue(entries.none { it.input == "M9" })
    }

    @Test
    fun deleteAndClearRecentKeepFavourites() {
        val repo = repository()
        repo.record(TextToMorse, "A", ".-")
        repo.record(TextToMorse, "B", "-...")
        repo.record(TextToMorse, "C", "-.-.")
        val (c, b, _) = repo.entries.value
        repo.setFavorite(b.id, true)
        repo.delete(c.id)
        assertEquals(listOf("B", "A"), repo.entries.value.map { it.input })
        repo.clearRecent()
        assertEquals(listOf("B"), repo.entries.value.map { it.input })
        repo.delete(b.id)
        assertEquals(emptyList(), repo.entries.value)
    }

    @Test
    fun historySurvivesARestartIncludingAwkwardText() {
        val awkward = "Line 1\nLine 2 \\ back\\slash \u001E rec \u001F field 👍 “quotes”"
        repository().record(TextToMorse, awkward, ".-..")
        val loaded = repository().entries.value.single()
        assertEquals(awkward, loaded.input)
        assertEquals(".-..", loaded.output)
    }

    @Test
    fun corruptOrUnknownDataStartsEmptyOrSkipsBadEntries() {
        store.putString(HistoryRepository.KEY_ENTRIES, "not history")
        assertEquals(emptyList(), repository().entries.value)
        val good = HistoryCodec.encode(listOf(HistoryEntry(7, TextToMorse, "OK", "--- -.-", 5)))
        store.putString(HistoryRepository.KEY_ENTRIES, good + "\u001Ebroken\u001Fentry" + "\u001E8\u001FSideways\u001F1\u001F0\u001Fx\u001Fy")
        assertEquals(listOf("OK"), repository().entries.value.map { it.input })
    }

    @Test
    fun codecRoundTripsEveryField() {
        val entries = listOf(
            HistoryEntry(1, TextToMorse, "a\\r", "\\f", 10, favorite = true),
            HistoryEntry(2, MorseToText, "", "", 0),
        )
        assertEquals(entries, HistoryCodec.decode(HistoryCodec.encode(entries)))
    }

    @Test
    fun restorePutsADeletedEntryBackInPlace() {
        val repo = repository()
        listOf("A", "B", "C").forEach { repo.record(TextToMorse, it, ".") }
        val b = repo.entries.value[1]
        repo.delete(b.id)
        repo.restore(b)
        assertEquals(listOf("C", "B", "A"), repo.entries.value.map { it.input })
        repo.restore(b)
        assertEquals(3, repo.entries.value.size, "restoring twice doesn't duplicate")
    }

    @Test
    fun starringSavesAndFavouritesWithoutDuplicates() {
        val repo = repository()
        repo.star(TextToMorse, "SOS", "... --- ...")
        val starred = repo.entries.value.single()
        assertTrue(starred.favorite)
        repo.record(TextToMorse, "SOS ", "... --- ...")
        repo.star(TextToMorse, " SOS", "... --- ...")
        assertEquals(listOf(starred.id), repo.entries.value.map { it.id })
        repo.setFavorite(starred.id, false)
        assertEquals(false, repo.entries.value.find(TextToMorse, "SOS")?.favorite, "un-starred, still in history")
    }

    @Test
    fun findMatchesDirectionAndTrimmedInput() {
        val repo = repository()
        repo.record(TextToMorse, "HI", ".... ..")
        assertEquals("HI", repo.entries.value.find(TextToMorse, " HI ")?.input)
        assertEquals(null, repo.entries.value.find(MorseToText, "HI"))
        assertEquals(null, repo.entries.value.find(TextToMorse, "HELLO"))
    }
}
