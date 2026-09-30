package `in`.geekofia.morsekit.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** One piece: launched from the burst point, then pulled down by gravity. Units are fractions of the area per second. */
internal data class ConfettiPiece(
    val velocityX: Float,
    val velocityY: Float,
    val spinDegreesPerSecond: Float,
    val sizeFraction: Float,
    val colorIndex: Int,
) {
    /** Where the piece is after [seconds], as a fraction of the area (0..1 is inside). */
    fun positionAt(seconds: Float): Offset = Offset(
        x = ORIGIN_X + velocityX * seconds,
        y = ORIGIN_Y + velocityY * seconds + GRAVITY * seconds * seconds / 2,
    )

    internal companion object {
        const val ORIGIN_X = 0.5f
        const val ORIGIN_Y = 0.4f
        // Strong enough that even the fastest piece peaks inside the area and lands below the burst
        // point before the animation ends (ConfettiTest checks both).
        const val GRAVITY = 2.6f
    }
}

/** Pieces fanned upwards (within 60° of straight up), at random speeds, spins, sizes and colours. */
internal fun confettiPieces(count: Int, colorCount: Int, random: Random): List<ConfettiPiece> = List(count) {
    val angle = -PI / 2 + (random.nextDouble() - 0.5) * (2 * PI / 3)
    val speed = 0.6 + random.nextDouble() * 0.7
    ConfettiPiece(
        velocityX = (cos(angle) * speed).toFloat(),
        velocityY = (sin(angle) * speed).toFloat(),
        spinDegreesPerSecond = (random.nextDouble() * 720 - 360).toFloat(),
        sizeFraction = (0.018 + random.nextDouble() * 0.02).toFloat(),
        colorIndex = random.nextInt(colorCount),
    )
}

private const val DURATION_MILLIS = 1_400
private const val PIECES = 48
/** The last part of the animation fades out, so pieces don't just vanish. */
private const val FADE_FROM = 0.7f

/**
 * A one-off burst of confetti in the theme's colours, drawn with a [Canvas] (no library). Decorative
 * only: it has no semantics and doesn't take touches. Plays once each time it enters composition.
 */
@Composable
fun ConfettiBurst(modifier: Modifier = Modifier, seed: Int = 0) {
    val colors = with(MaterialTheme.colorScheme) { listOf(primary, secondary, tertiary, error, primaryContainer) }
    val pieces = remember(seed) { confettiPieces(PIECES, colors.size, Random(seed)) }
    val progress = remember(seed) { Animatable(0f) }
    LaunchedEffect(seed) { progress.animateTo(1f, tween(DURATION_MILLIS, easing = LinearEasing)) }

    Canvas(modifier) {
        val t = progress.value
        if (t >= 1f) return@Canvas
        val seconds = t * DURATION_MILLIS / 1_000f
        val alpha = if (t < FADE_FROM) 1f else (1f - t) / (1f - FADE_FROM)
        pieces.forEach { piece ->
            val p = piece.positionAt(seconds)
            val center = Offset(p.x * size.width, p.y * size.height)
            val side = piece.sizeFraction * size.minDimension
            rotate(piece.spinDegreesPerSecond * seconds, pivot = center) {
                drawRect(
                    color = colors[piece.colorIndex],
                    topLeft = Offset(center.x - side / 2, center.y - side / 4),
                    size = Size(side, side / 2),
                    alpha = alpha,
                )
            }
        }
    }
}
