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
            "knight" -> Knight(side)
            "bishop" -> Bishop(side)
            "rook" -> Rook(side)
            "king" -> King(side)
            "pawn" -> Pawn(side)
            "queen" -> Queen(side)
            else -> throw IllegalArgumentException("unknown piece")
        }
        //TODO("furhter pieces")
        val square = Square.of(tsquare)

        board.addPiece(square, piece)
    }
}