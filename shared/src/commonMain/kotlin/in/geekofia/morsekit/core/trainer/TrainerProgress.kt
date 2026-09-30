package `in`.geekofia.morsekit.core.trainer

/** How often one character was answered right and wrong. */
data class CharStats(val correct: Int = 0, val wrong: Int = 0) {
    init {
        require(correct >= 0 && wrong >= 0) { "Counts can't be negative" }
    }

    val attempts: Int get() = correct + wrong

    /** Share of wrong answers, 0 when there are none yet. */
    val errorRate: Double get() = if (attempts == 0) 0.0 else wrong.toDouble() / attempts
}

/**
 * Where a learner is: how many characters of [KochOrder] are unlocked ([level]), per-character
 * [stats], and the [recent] answers (oldest first) that decide the next unlock.
 */
data class TrainerProgress(
    val level: Int = KochOrder.START_LEVEL,
    val stats: Map<Char, CharStats> = emptyMap(),
    val recent: List<Boolean> = emptyList(),
) {
    init {
        require(level in KochOrder.START_LEVEL..KochOrder.characters.size) { "Level out of range: $level" }
        require(recent.size <= KochProgression.WINDOW) { "Too many recent answers: ${recent.size}" }
    }

    val unlocked: List<Char> get() = KochOrder.characters.take(level)

    /** The most recently unlocked character, practised more often. */
    val newest: Char get() = unlocked.last()

    val isComplete: Boolean get() = level == KochOrder.characters.size

    val recentCorrect: Int get() = recent.count { it }
}
