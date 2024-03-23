package net.binarysailor.chessengine

import net.binarysailor.chesslounge.engine.Board
import net.binarysailor.chesslounge.engine.BoardModification
import net.binarysailor.chesslounge.engine.IllegalMoveReason
import net.binarysailor.chesslounge.engine.Move
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class PieceMoveTest {

    private val initialPiecePlacements: MutableList<String> = mutableListOf()
    private var previousMoveSymbol: String? = null
    private var moveSymbols: MutableList<String> = mutableListOf()
    private var expectedIllegalMoveReason: IllegalMoveReason? = null
    private var expectedSideEffect: BoardModification? = null

    fun setup(description: String) = apply { initialPiecePlacements.addAll(description.split(",").map { it.trim() }) }
    fun previousMove(move: String) = apply { this.previousMoveSymbol = move }
    fun move(move: String) = apply { this.moveSymbols.add(move) }

    fun moves(vararg moves: String) = apply { this.moveSymbols.addAll(moves) }

    fun expectedLegal() = apply { this.expectedIllegalMoveReason = null }
    fun expectedIllegalBecauseOf(reason: IllegalMoveReason) = apply { this.expectedIllegalMoveReason = reason }
    fun expectedSideEffect(sideEffect: BoardModification) = apply { this.expectedSideEffect = sideEffect }
    fun expectedSideEffect(moveSymbol: String) = apply { this.expectedSideEffect = Move.parse(moveSymbol) }

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
                val move = Move.parse(it)
                val piece = board.pieceAt(move.from) ?: throw IllegalArgumentException("No piece on ${move.from}")
                val history = moveHistory {
                    withLastMove(previousMoveSymbol?.let { s -> Move.parse(s) })
                }

                // when
                val response = piece.tryMove(board, history, move)

                // then
                if (expectedIllegalMoveReason == null) {
                    assert(response.legality.legal) { "Move $it should be legal" }
                    if (expectedSideEffect != null) {
                        assertFalse(response.sideEffects.isEmpty(), "A side effect was expected")
                        assertEquals(expectedSideEffect, response.sideEffects[0])
                    }
                } else {
                    assertEquals(expectedIllegalMoveReason, response.legality.illegalReason,
                        "Move $it should be illegal because of $expectedIllegalMoveReason")
                }
            }
        }
    }
}