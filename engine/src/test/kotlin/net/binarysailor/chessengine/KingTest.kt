package net.binarysailor.chessengine

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class KingTest {

    @TestFactory
    fun `should reject illegal moves`(): List<DynamicTest> {
        return listOf(
            PieceMoveTest()
                .setup("white king C4, white rook C5")
                .move("C4-C5")
                .expectedIllegalBecauseOf(IllegalMoveReason.FRIEND_PIECE_ON_WAY),
            PieceMoveTest()
                .setup("white king C4, white rook D4")
                .move("C4-D4")
                .expectedIllegalBecauseOf(IllegalMoveReason.FRIEND_PIECE_ON_WAY),
            PieceMoveTest()
                .setup("white king C4")
                .move("C4-C6")
                .expectedIllegalBecauseOf(IllegalMoveReason.ILLEGAL_MOVE_SHAPE)
        ).map { it.build() }
    }

    @TestFactory
    fun `should allow simple legal moves`(): List<DynamicTest> {
        val position = "white king C4"
        val moves = listOf("C5", "D5", "D4", "D3", "C3", "B3", "B4", "B5").map {
            PieceMoveTest()
                .setup(position)
                .move("C4-${it}")
                .expectedLegal()
        }
        return moves.map { it.build() }
    }

    @TestFactory
    fun `should allow short castles`(): List<DynamicTest> {
        return listOf(
            PieceMoveTest()
                .setup("white king E1, white rook H1")
                .move("E1-G1")
                .expectedLegal(),
        ).map { it.build() }
    }
}