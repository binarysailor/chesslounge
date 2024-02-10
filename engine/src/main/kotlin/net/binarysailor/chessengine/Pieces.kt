package net.binarysailor.chessengine

import kotlin.math.max

internal interface Piece {
    val side: Side
    fun canMove(board: Board, history: MoveHistory, move: Move): IllegalMoveReason?
}

enum class IllegalMoveReason {
    ILLEGAL_MOVE_SHAPE, ENEMY_PIECE_ON_WAY, FRIEND_PIECE_ON_WAY
}

class Rook(override val side: Side) : Piece {

    override fun canMove(board: Board, history: MoveHistory, move: Move): IllegalMoveReason? {
        if (
        // move along a file
        move.fileDistance() == 0 && move.rankDistance() > 0
        || // move along a rank
        move.rankDistance() == 0 && move.fileDistance() > 0
            ) {
            return board.findObstacle(move.path(), side)
        }
        return IllegalMoveReason.ILLEGAL_MOVE_SHAPE
    }

    override fun equals(other: Any?): Boolean {
        return other is Rook && other.side == this.side
    }
}

class King(override val side: Side) : Piece {
    override fun canMove(board: Board, history: MoveHistory, move: Move): IllegalMoveReason? {
        if (max(move.rankDistance(), move.fileDistance()) == 1) {
            return board.findObstacle(move.path(), side)
        }

        val castle = asCastle(board, move) ?: return IllegalMoveReason.ILLEGAL_MOVE_SHAPE
        return if (canCastle(castle, history)) null else IllegalMoveReason.ILLEGAL_MOVE_SHAPE
    }

    private fun asCastle(board: Board, move: Move): Castle? {
        val startingSquare = startingSquare(side)
        if (move.from != startingSquare) {
            return null
        }
        if (move.to != startingSquare.withFile('C') && move.to != startingSquare.withFile('G')) {
            return null
        }
        val expectedRookSquare = when (move.to) {
            startingSquare.withFile('C') -> startingSquare.withFile('A')
            startingSquare.withFile('G') -> startingSquare.withFile('H')
            else -> null
        } ?: return null

        if (board.pieceAt(expectedRookSquare) != Rook(side)) {
            return null
        }

        if (board.findObstacle(Move(move.from, expectedRookSquare).path().dropLast(1), side) != null) {
            return null
        }

        return Castle(side, expectedRookSquare.file)
    }

    private fun canCastle(castle: Castle, history: MoveHistory): Boolean {
        return !history.hasMovesFrom(startingSquare(castle.side)) && !history.hasMovesFrom(castle.rookFrom())
    }

    private data class Castle(val side: Side, val rookFile: Int) {
        fun rookFrom() = startingSquare(side).withFile(rookFile)
    }

    companion object {
        fun startingSquare(side: Side) = Square.of(if (side == Side.WHITE) "E1" else "E8")
    }

}