package `in`.geekofia.morsekit.core.trainer

/**
 * One sitting of practice. With a [length] it's a counted session (a summary comes after the
 * last question); without one it's endless practice, and only the score shows.
 */
data class PracticeSession(
    val length: Int? = null,
    val answered: Int = 0,
    val correct: Int = 0,
    val streak: Int = 0,
    /** Characters answered wrongly, and how often. */
    val misses: Map<Char, Int> = emptyMap(),
    /** Characters unlocked during the session, in order. */
    val unlocked: List<Char> = emptyList(),
) {
    init {
        require(length == null || length > 0) { "A session needs at least one question" }
    }

    val isCounted: Boolean get() = length != null

    val isFinished: Boolean get() = length != null && answered >= length

    /** Rounded to a whole percent; `null` before the first answer. */
    val accuracyPercent: Int? get() = if (answered == 0) null else (correct * 100 + answered / 2) / answered

    /** Misses, most frequent first (then in [KochOrder] order). */
    val missesByCount: List<Pair<Char, Int>>
        get() = misses.toList().sortedWith(compareByDescending<Pair<Char, Int>> { it.second }.thenBy { KochOrder.characters.indexOf(it.first) })

    fun record(target: Char, correct: Boolean, unlockedChar: Char? = null): PracticeSession = copy(
        answered = answered + 1,
        correct = this.correct + if (correct) 1 else 0,
        streak = if (correct) streak + 1 else 0,
        misses = if (correct) misses else misses + (target to (misses[target] ?: 0) + 1),
        unlocked = unlocked + listOfNotNull(unlockedChar),
    )

    companion object {
        val LENGTHS = listOf(10, 20, 50)
        const val DEFAULT_LENGTH = 20
    }
}
