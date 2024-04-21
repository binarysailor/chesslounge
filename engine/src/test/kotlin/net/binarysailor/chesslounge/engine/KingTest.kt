package net.binarysailor.chesslounge.engine

import net.binarysailor.chesslounge.engine.IllegalMoveReason
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class KingTest {

    @TestFactory
    fun `should reject illegal moves`(): List<DynamicTest> {
        return listOf(
            PieceMoveTest()
                .setup("white king C4, white rook C5")
                .move("C4-C5")
                .expectedIllegalBecauseOf(IllegalMoveReason.FRIEND_PIECE_IN_THE_WAY),
            PieceMoveTest()
                .setup("white king C4, white rook D4")
                .move("C4-D4")
                .expectedIllegalBecauseOf(IllegalMoveReason.FRIEND_PIECE_IN_THE_WAY),
            PieceMoveTest()
                .setup("white king C4")
                .move("C4-C6")
                .expectedIllegalBecauseOf(IllegalMoveReason.ILLEGAL_MOVE_SHAPE),
            PieceMoveTest()
                .setup("white king C4, white rook D5")
                .move("C4-D5")
                .expectedIllegalBecauseOf(IllegalMoveReason.FRIEND_PIECE_IN_THE_WAY)
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
    fun `should allow takes`(): List<DynamicTest> {
        return listOf(
            PieceMoveTest()
                .setup("black king D7, white rook E6")
                .move("D7-E6")
                .expectedLegal()
                .build(),
            PieceMoveTest()
                .setup("black king D7, black rook D8, white rook D6")
                .move("D7-D6")
                .expectedLegal()
                .build()
        )
    }

    @TestFactory
    fun `should allow short castles`(): List<DynamicTest> {
        return listOf(
            PieceMoveTest()
                .setup("white king E1, white rook H1")
                .move("E1-G1")
                .expectedLegal()
                .expectedSideEffect("H1-F1"),
        ).map { it.build() }
    }

    @TestFactory
    fun `should allow long castles`(): List<DynamicTest> {
        return listOf(
            PieceMoveTest()
                .setup("white king E1, white rook A1, white rook G1")
                .move("E1-C1")
                .expectedLegal()
                .expectedSideEffect("A1-D1"),
        ).map { it.build() }
    }
}