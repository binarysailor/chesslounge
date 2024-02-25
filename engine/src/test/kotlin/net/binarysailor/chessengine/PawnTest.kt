package net.binarysailor.chessengine

import net.binarysailor.chesslounge.engine.IllegalMoveReason.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class PawnTest {

    @TestFactory
    fun `should allow one square forward`(): List<DynamicTest> {
        return "abcdefgh".flatMap {
            file -> "234567".map {
                rank -> PieceMoveTest()
                    .setup("white pawn ${file}${rank}")
                    .move("${file}${rank}-${file}${rank+1}")
                    .expectedLegal()
                    .build()
            }
        }
    }

    @TestFactory
    fun `should not allow one square forward if occupied`(): List<DynamicTest> {
        return listOf(
            PieceMoveTest()
                .setup("white pawn D3, white pawn D4")
                .move("D3-D4")
                .expectedIllegalBecauseOf(FRIEND_PIECE_IN_THE_WAY)
                .build(),
            PieceMoveTest()
                .setup("black pawn D6, white rook D5")
                .move("D6-D5")
                .expectedIllegalBecauseOf(OPPONENT_PIECE_IN_THE_WAY)
                .build()
        )
    }

    @TestFactory
    fun `should allow two squares forward`(): List<DynamicTest> {
        return listOf(
            PieceMoveTest()
                .setup("white pawn C2")
                .move("C2-C4")
                .expectedLegal()
                .build(),
            PieceMoveTest()
                .setup("black pawn G7")
                .move("G7-G5")
                .expectedLegal()
                .build()
        )
    }

    @TestFactory
    fun `should not allow two squares forward`(): List<DynamicTest> {
        return listOf(
            PieceMoveTest()
                .setup("white pawn A1")
                .move("A1-A3")
                .expectedIllegalBecauseOf(ILLEGAL_MOVE_SHAPE)
                .build(),
            PieceMoveTest()
                .setup("white pawn A2, white rook A3")
                .move("A2-A4")
                .expectedIllegalBecauseOf(FRIEND_PIECE_IN_THE_WAY)
                .build(),
            PieceMoveTest()
                .setup("white pawn A2, white rook A4")
                .move("A2-A4")
                .expectedIllegalBecauseOf(FRIEND_PIECE_IN_THE_WAY)
                .build(),
            PieceMoveTest()
                .setup("white pawn A2, black king A4")
                .move("A2-A4")
                .expectedIllegalBecauseOf(OPPONENT_PIECE_IN_THE_WAY)
                .build(),
            PieceMoveTest()
                .setup("white pawn A7")
                .move("A7-A5")
                .expectedIllegalBecauseOf(ILLEGAL_MOVE_SHAPE)
                .build()
        )
    }

    @TestFactory
    fun `should allow takes`(): List<DynamicTest> {
        return listOf(
            PieceMoveTest()
                .setup("white pawn E4, black pawn F5")
                .move("E4-F5")
                .expectedLegal()
                .build(),
            PieceMoveTest()
                .setup("black pawn A7, white pawn B6")
                .move("A7-B6")
                .expectedLegal()
                .build(),
            PieceMoveTest()
                .setup("black pawn C4, white pawn B4")
                .previousMove("B2-B4")
                .move("C4-B3")
                .expectedLegal()
                .build()
        )
    }

    @TestFactory
    fun `should not allow takes`(): List<DynamicTest> {
        return listOf(
            PieceMoveTest()
                .setup("white pawn E4")
                .move("E4-F5")
                .expectedIllegalBecauseOf(ILLEGAL_MOVE_SHAPE)
                .build(),
            PieceMoveTest()
                .setup("white pawn E4, white pawn F5")
                .move("E4-F5")
                .expectedIllegalBecauseOf(FRIEND_PIECE_IN_THE_WAY)
                .build(),
            PieceMoveTest()
                .setup("white pawn G3, black pawn G4")
                .move("G3-G4")
                .expectedIllegalBecauseOf(OPPONENT_PIECE_IN_THE_WAY)
                .build(),
            PieceMoveTest()
                .setup("white pawn G5, black pawn H5")
                .move("G5-H6")
                .expectedIllegalBecauseOf(ILLEGAL_MOVE_SHAPE)
                .build()
        )
    }
}