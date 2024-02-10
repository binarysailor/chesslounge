package net.binarysailor.chessengine

internal class TextualPositionSetup(val board: Board) {

    /**
     * interprets descriptions like "white rook A1"
     */
    internal fun add(description: String) {
        val (tside, tpiece, tsquare) = description.split(' ')
        val side = Side.valueOf(tside.uppercase())
        val piece: Piece = when {
            tpiece == "rook" -> Rook(side)
            tpiece == "king" -> King(side)
            else -> throw IllegalArgumentException("unknown piece")
        }
        //TODO("furhter pieces")
        val square = Square.of(tsquare)

        board.addPiece(square, piece)
    }
}