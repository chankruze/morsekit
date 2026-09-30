package `in`.geekofia.morsekit.core.trainer

/**
 * The unlock rule: once at least [UNLOCK_CORRECT] of the last [WINDOW] answers are right (90%),
 * the next character of [KochOrder] is unlocked and the window starts over, so the new character
 * is part of what's measured next.
 */
object KochProgression {
    const val WINDOW = 20
    const val UNLOCK_CORRECT = 18

    data class Result(
        val progress: TrainerProgress,
        /** The character this answer unlocked, if any. */
        val unlocked: Char?,
    )

    fun record(progress: TrainerProgress, target: Char, correct: Boolean): Result {
        val stats = progress.stats[target] ?: CharStats()
        val updated = progress.copy(
            stats = progress.stats + (target to if (correct) stats.copy(correct = stats.correct + 1) else stats.copy(wrong = stats.wrong + 1)),
            recent = (progress.recent + correct).takeLast(WINDOW),
        )
        val unlocks = !updated.isComplete && updated.recent.size == WINDOW && updated.recentCorrect >= UNLOCK_CORRECT
        if (!unlocks) return Result(updated, unlocked = null)
        val next = updated.copy(level = updated.level + 1, recent = emptyList())
        return Result(next, unlocked = next.newest)
    }
}
