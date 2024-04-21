package net.binarysailor.chesslounge.engine

import net.binarysailor.chesslounge.engine.IllegalMoveReason
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class BishopTest {

    @TestFactory
    fun `should reject illegal moves`(): List<DynamicTest> {
        return listOf(

            PieceMoveTest()
                .setup("white bishop B6, white king F2, black knight E3")
                .move("B6-F2")
                .expectedIllegalBecauseOf(IllegalMoveReason.OPPONENT_PIECE_IN_THE_WAY),
            PieceMoveTest()
                .setup("white bishop B6, white king F2, black knight E3")
                .move("B6-C8")
                .expectedIllegalBecauseOf(IllegalMoveReason.ILLEGAL_MOVE_SHAPE),
            PieceMoveTest()
                .setup("white bishop B6, white rook C7")
                .moves("B6-C7", "B6-D8")
                .expectedIllegalBecauseOf(IllegalMoveReason.FRIEND_PIECE_IN_THE_WAY)
        ).map { it.build() }
    }

    @TestFactory
    fun `should allow legal moves`(): List<DynamicTest> {
        return listOf(
            PieceMoveTest()
                .setup("white bishop B6, white king F2, black knight E3")
                .moves("B6-C5", "B6-D4", "B6-E3", "B6-A7", "B6-C7", "B6-D8")
                .expectedLegal()
        ).map { it.build() }
    }
}