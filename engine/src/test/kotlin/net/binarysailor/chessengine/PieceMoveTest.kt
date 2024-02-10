package net.binarysailor.chessengine

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import kotlin.test.assertEquals

class PieceMoveTest {

    private val setup: MutableList<String> = mutableListOf()
    private lateinit var moveSymbol: String
    private var expectedIllegalMoveReason: IllegalMoveReason? = null

    fun setup(description: String) = apply { setup.addAll(description.split(",").map { it.trim() }) }
    fun move(move: String) = apply { this.moveSymbol = move }

    fun expectedLegal() = apply { this.expectedIllegalMoveReason = null }
    fun expectedIllegalBecauseOf(reason: IllegalMoveReason) = apply { this.expectedIllegalMoveReason = reason }

    fun build(): DynamicTest {
        val testName = "Move $moveSymbol should be " +
                if (expectedIllegalMoveReason == null) {
                    "legal"
                } else {
                    "illegal because of $expectedIllegalMoveReason"
                }

        return dynamicTest(testName) {
            // given
            val board = Board()
            val util = TextualPositionSetup(board)
            setup.forEach { util.add(it) }

            val move = moveSymbol.parseAsMove()
            val piece = board.pieceAt(move.from) ?: throw IllegalArgumentException("No piece on ${move.from}")

            val history = TestMoveHistory()
            // when
            val illegalReason = piece.canMove(board, history , move)

            // then
            if (expectedIllegalMoveReason == null) {
                assertNull(illegalReason)
            } else {
                assertEquals(expectedIllegalMoveReason, illegalReason)
            }
        }
    }

    private fun String.parseAsMove(): Move {
        val parts = split("-")
        return Move(Square.of(parts[0]), Square.of(parts[1]))
    }
}