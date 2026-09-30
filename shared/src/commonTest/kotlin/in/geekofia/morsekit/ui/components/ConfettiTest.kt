package `in`.geekofia.morsekit.ui.components

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConfettiTest {
    private val pieces = confettiPieces(count = 200, colorCount = 5, random = Random(3))

    @Test
    fun everyPieceStartsAtTheBurstPoint() {
        pieces.forEach {
            assertEquals(ConfettiPiece.ORIGIN_X, it.positionAt(0f).x)
            assertEquals(ConfettiPiece.ORIGIN_Y, it.positionAt(0f).y)
        }
    }

    @Test
    fun piecesGoUpFirstThenFallBelowTheStart() {
        pieces.forEach {
            assertTrue(it.velocityY < 0, "launched upwards")
            assertTrue(it.positionAt(0.1f).y < ConfettiPiece.ORIGIN_Y)
            assertTrue(it.positionAt(1.4f).y > ConfettiPiece.ORIGIN_Y, "gravity wins by the end")
            val apexSeconds = -it.velocityY / ConfettiPiece.GRAVITY
            assertTrue(it.positionAt(apexSeconds).y >= 0f, "peaks inside the area, not off the top")
        }
    }

    @Test
    fun colorsAndSizesAreInRange() {
        assertTrue(pieces.all { it.colorIndex in 0 until 5 })
        assertTrue(pieces.all { it.sizeFraction > 0f })
        assertEquals(pieces, confettiPieces(count = 200, colorCount = 5, random = Random(3)))
    }
}
