package `in`.geekofia.morsekit.core.trainer

import `in`.geekofia.morsekit.platform.KeyValueStore

/**
 * Saves [TrainerProgress] and the trainer's own options in the [KeyValueStore]. Anything missing,
 * corrupt or out of range falls back to a fresh start rather than crashing: progress is nice to
 * keep, not worth failing over.
 */
class TrainerRepository(private val store: KeyValueStore) {

    fun load(): TrainerProgress {
        val level = store.getInt(KEY_LEVEL)?.coerceIn(KochOrder.START_LEVEL, KochOrder.characters.size)
            ?: KochOrder.START_LEVEL
        return TrainerProgress(
            level = level,
            stats = decodeStats(store.getString(KEY_STATS).orEmpty()),
            recent = store.getString(KEY_RECENT).orEmpty()
                .mapNotNull { when (it) { '1' -> true; '0' -> false; else -> null } }
                .takeLast(KochProgression.WINDOW),
        )
    }

    fun save(progress: TrainerProgress) {
        store.putInt(KEY_LEVEL, progress.level)
        store.putString(KEY_STATS, encodeStats(progress.stats))
        store.putString(KEY_RECENT, progress.recent.joinToString("") { if (it) "1" else "0" })
    }

    fun reset() = save(TrainerProgress())

    var hideMorse: Boolean
        get() = store.getBoolean(KEY_HIDE_MORSE) ?: false
        set(value) = store.putBoolean(KEY_HIDE_MORSE, value)

    var mode: TrainerMode
        get() = store.getString(KEY_MODE)?.let { name -> TrainerMode.entries.firstOrNull { it.name == name } }
            ?: TrainerMode.Listen
        set(value) = store.putString(KEY_MODE, value.name)

    /** The last session length chosen; always one of [PracticeSession.LENGTHS]. */
    var sessionLength: Int
        get() = store.getInt(KEY_SESSION_LENGTH)?.takeIf { it in PracticeSession.LENGTHS }
            ?: PracticeSession.DEFAULT_LENGTH
        set(value) = store.putInt(KEY_SESSION_LENGTH, value)

    internal companion object {
        const val KEY_LEVEL = "trainer.level"
        const val KEY_STATS = "trainer.stats"
        const val KEY_RECENT = "trainer.recent"
        const val KEY_HIDE_MORSE = "trainer.hideMorse"
        const val KEY_MODE = "trainer.mode"
        const val KEY_SESSION_LENGTH = "trainer.sessionLength"

        // "K3:1;M5:0": the character, then correct:wrong. ';' and ':' aren't in KochOrder, so a
        // character like ',' or '/' can't be confused with a separator.
        fun encodeStats(stats: Map<Char, CharStats>): String =
            stats.entries.joinToString(";") { (char, s) -> "$char${s.correct}:${s.wrong}" }

        fun decodeStats(encoded: String): Map<Char, CharStats> = encoded.split(';').mapNotNull { entry ->
            val char = entry.firstOrNull()?.takeIf { it in KochOrder.characters } ?: return@mapNotNull null
            val counts = entry.drop(1).split(':').map { it.toIntOrNull() }
            val correct = counts.getOrNull(0)?.takeIf { it >= 0 } ?: return@mapNotNull null
            val wrong = counts.getOrNull(1)?.takeIf { it >= 0 } ?: return@mapNotNull null
            char to CharStats(correct, wrong)
        }.toMap()
    }
}
