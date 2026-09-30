package `in`.geekofia.morsekit.core.trainer

import `in`.geekofia.morsekit.platform.InMemoryKeyValueStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TrainerRepositoryTest {
    private val store = InMemoryKeyValueStore()
    private val repository = TrainerRepository(store)

    @Test
    fun emptyStoreIsAFreshStart() {
        assertEquals(TrainerProgress(), repository.load())
        assertFalse(repository.hideMorse)
    }

    @Test
    fun progressRoundTrips() {
        val progress = TrainerProgress(
            level = 12,
            stats = mapOf('K' to CharStats(5, 1), ',' to CharStats(2, 3), '/' to CharStats(0, 4), '.' to CharStats(1, 0)),
            recent = listOf(true, false, true, true),
        )
        repository.save(progress)
        assertEquals(progress, TrainerRepository(store).load())
    }

    @Test
    fun corruptValuesFallBackSafely() {
        store.putInt(TrainerRepository.KEY_LEVEL, 99)
        store.putString(TrainerRepository.KEY_STATS, "K3:1;;Z;éx:1;M-1:0;R2:x;S4:2")
        store.putString(TrainerRepository.KEY_RECENT, "1x0" + "1".repeat(40))
        val loaded = repository.load()
        assertEquals(KochOrder.characters.size, loaded.level)
        assertEquals(mapOf('K' to CharStats(3, 1), 'S' to CharStats(4, 2)), loaded.stats)
        assertEquals(KochProgression.WINDOW, loaded.recent.size)
    }

    @Test
    fun resetStartsOverAndHideMorseIsRemembered() {
        repository.save(TrainerProgress(level = 20, recent = listOf(true)))
        repository.hideMorse = true
        repository.reset()
        assertEquals(TrainerProgress(), repository.load())
        assertTrue(TrainerRepository(store).hideMorse)
    }
}
