package `in`.geekofia.morsekit.core.trainer

import kotlin.random.Random

/** One question: which character is this Morse? [choices] includes [target]. */
data class TrainerQuestion(val target: Char, val choices: List<Char>)

/**
 * Picks what to ask next from the unlocked characters. Weighted, not uniform:
 *
 * - each character weighs `1 + ERROR_WEIGHT × its error rate`, so the ones you miss come back;
 * - the newest character gets [NEWEST_BONUS] more, so a new character is practised straight away;
 * - with three or more unlocked, the previous target isn't asked again right away.
 *
 * Up to [CHOICES] choices: the target plus other unlocked characters, shuffled. [random] is
 * injectable so tests are repeatable.
 */
class QuestionGenerator(private val random: Random = Random.Default) {

    fun next(progress: TrainerProgress, previous: Char? = null): TrainerQuestion {
        val candidates = progress.unlocked.filter { progress.unlocked.size < NO_REPEAT_FROM || it != previous }
        val target = pickWeighted(candidates) { weight(progress, it) }
        val others = (progress.unlocked - target).shuffled(random).take(CHOICES - 1)
        return TrainerQuestion(target, (others + target).shuffled(random))
    }

    private fun weight(progress: TrainerProgress, char: Char): Double =
        1.0 + ERROR_WEIGHT * (progress.stats[char]?.errorRate ?: 0.0) + if (char == progress.newest) NEWEST_BONUS else 0.0

    private fun <T> pickWeighted(items: List<T>, weight: (T) -> Double): T {
        val weights = items.map(weight)
        var roll = random.nextDouble() * weights.sum()
        items.forEachIndexed { i, item ->
            roll -= weights[i]
            if (roll < 0) return item
        }
        return items.last()
    }

    companion object {
        const val CHOICES = 4
        const val ERROR_WEIGHT = 3.0
        const val NEWEST_BONUS = 2.0
        private const val NO_REPEAT_FROM = 3
    }
}
