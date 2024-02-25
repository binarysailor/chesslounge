package net.binarysailor.chessengine

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class KnightTest {

    @TestFactory
    fun `should reject illegal moves`(): List<DynamicTest> {
        return listOf(
            PieceMoveTest()
                .setup("white knight B2, white pawn B3, white pawn C3, white king C2")
                .move("B2-A1")
                .expectedIllegalBecauseOf(IllegalMoveReason.ILLEGAL_MOVE_SHAPE),
            PieceMoveTest()
                .setup("white knight B2, white pawn B3, white pawn C4")
                .move("B2-C4")
                .expectedIllegalBecauseOf(IllegalMoveReason.FRIEND_PIECE_IN_THE_WAY),
        ).map { it.build() }
    }

    @TestFactory
    fun `should allow simple legal moves`(): List<DynamicTest> {
        return listOf(
            PieceMoveTest()
                .setup("white knight B2, white pawn B3, white pawn C3, white king C2")
                .moves("B2-C4", "B2-D3", "B2-D1", "B2-A4", "B2-C3")
                .expectedLegal()
        ).map { it.build() }
    }

    @TestFactory
    fun `should allow takes`(): List<DynamicTest> {
        return listOf(
            PieceMoveTest()
                .setup("black knight D7, white rook F8")
                .move("D7-F8")
                .expectedLegal()
                .build(),
        )
    }
}