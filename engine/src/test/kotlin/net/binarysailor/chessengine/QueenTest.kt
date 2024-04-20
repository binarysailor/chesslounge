package net.binarysailor.chessengine

import net.binarysailor.chesslounge.engine.IllegalMoveReason
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class QueenTest {

    @TestFactory
    fun `should reject illegal moves`(): List<DynamicTest> {
        return listOf(
            PieceMoveTest()
                .setup("white queen A1, white king G1")
                .move("A1-G1")
                .expectedIllegalBecauseOf(IllegalMoveReason.FRIEND_PIECE_IN_THE_WAY),
            PieceMoveTest()
                .setup("white queen A1, white king G1")
                .move("A1-H1")
                .expectedIllegalBecauseOf(IllegalMoveReason.FRIEND_PIECE_IN_THE_WAY),
            PieceMoveTest()
                .setup("white queen A1, white king G1, black rook A3")
                .move("A1-A4")
                .expectedIllegalBecauseOf(IllegalMoveReason.OPPONENT_PIECE_IN_THE_WAY),
            PieceMoveTest()
                .setup("white queen A1, white king G1, black rook A3")
                .move("A1-B4")
                .expectedIllegalBecauseOf(IllegalMoveReason.ILLEGAL_MOVE_SHAPE)
        ).map { it.build() }
    }

    @TestFactory
    fun `should allow legal moves`(): List<DynamicTest> {
        val position = "white queen B1, white king D2, black rook H1, black rook B8"
        val rankMoves = "ACDEFGH".toCharArray().map {
            PieceMoveTest()
                .setup(position)
                .move("B1-${it}1")
                .expectedLegal()
        }
        val fileMoves = "2345678".toCharArray().map {
            PieceMoveTest()
                .setup(position)
                .move("B1-B$it")
                .expectedLegal()

        }
        val diagonalMoves = listOf("A2", "C2", "D3", "E4", "F5", "G6", "H7").map {
            PieceMoveTest()
                .setup(position)
                .move("B1-$it")
                .expectedLegal()
        }
        return (rankMoves + fileMoves + diagonalMoves).map { it.build() }
    }
}