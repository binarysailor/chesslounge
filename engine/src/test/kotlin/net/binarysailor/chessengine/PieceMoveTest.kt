package net.binarysailor.chessengine

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import kotlin.test.assertEquals

class PieceMoveTest {

    private val initialPiecePlacements: MutableList<String> = mutableListOf()
    private var previousMoveSymbol: String? = null
    private var moveSymbols: MutableList<String> = mutableListOf()
    private var expectedIllegalMoveReason: IllegalMoveReason? = null

    fun setup(description: String) = apply { initialPiecePlacements.addAll(description.split(",").map { it.trim() }) }
    fun previousMove(move: String) = apply { this.previousMoveSymbol = move }
    fun move(move: String) = apply { this.moveSymbols.add(move) }

    fun moves(vararg moves: String) = apply { this.moveSymbols.addAll(moves) }

    fun expectedLegal() = apply { this.expectedIllegalMoveReason = null }
    fun expectedIllegalBecauseOf(reason: IllegalMoveReason) = apply { this.expectedIllegalMoveReason = reason }

    fun build(): DynamicTest {
        val testName = "Move${if (moveSymbols.size > 1) "s" else ""} $moveSymbols should be " +
                if (expectedIllegalMoveReason == null) {
                    "legal"
                } else {
                    "illegal because of $expectedIllegalMoveReason"
                }

        return dynamicTest(testName) {
            // given
            val board = Board()
            val positionSetup = TextualPositionSetup(board)
            initialPiecePlacements.forEach { positionSetup.placePiece(it) }

            moveSymbols.forEach {
                val move = it.parseAsMove()
                val piece = board.pieceAt(move.from) ?: throw IllegalArgumentException("No piece on ${move.from}")
                val history = moveHistory {
                    withLastMove(previousMoveSymbol?.parseAsMove())
                }

                // when
                val legality = piece.canMove(board, history, move)

                // then
                if (expectedIllegalMoveReason == null) {
                    assert(legality.legal()) { "Move $it should be legal" }
                } else {
                    assertEquals(expectedIllegalMoveReason, legality.illegalReason,
                        "Move $it should be illegal because of $expectedIllegalMoveReason")
                }
            }
        }
    }

    private fun String.parseAsMove(): Move {
        val parts = split("-")
        return Move(Square.of(parts[0]), Square.of(parts[1]))
    }
}