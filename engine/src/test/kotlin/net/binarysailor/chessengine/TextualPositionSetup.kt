package net.binarysailor.chessengine

import net.binarysailor.chesslounge.engine.*
import net.binarysailor.chesslounge.engine.Piece

internal class TextualPositionSetup(val board: Board) {

    /**
     * interprets descriptions like "white rook A1"
     */
    internal fun placePiece(description: String) {
        val (tside, tpiece, tsquare) = description.split(' ')
        val side = Side.valueOf(tside.uppercase())
        val piece: Piece = when (tpiece) {
            "knight" -> net.binarysailor.chesslounge.engine.Knight(side)
            "bishop" -> net.binarysailor.chesslounge.engine.Bishop(side)
            "rook" -> net.binarysailor.chesslounge.engine.Rook(side)
            "king" -> net.binarysailor.chesslounge.engine.King(side)
            "pawn" -> net.binarysailor.chesslounge.engine.Pawn(side)
            else -> throw IllegalArgumentException("unknown piece")
        }
        //TODO("furhter pieces")
        val square = Square.of(tsquare)

        board.addPiece(square, piece)
    }
}