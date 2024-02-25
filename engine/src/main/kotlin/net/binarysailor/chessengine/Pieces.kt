package net.binarysailor.chessengine

import net.binarysailor.chessengine.IllegalMoveReason.FRIEND_PIECE_IN_THE_WAY
import net.binarysailor.chessengine.IllegalMoveReason.ILLEGAL_MOVE_SHAPE
import net.binarysailor.chessengine.MoveLegality.Companion.illegalBecause
import net.binarysailor.chessengine.MoveLegality.Companion.legal
import kotlin.math.max

internal interface Piece {
    val side: Side
    fun canMove(board: Board, history: MoveHistory, move: Move): MoveLegality
    fun checkPathClear(board: Board, move: Move, takeIsAllowed: Boolean = true) =
        board.findObstacle(move.path(), side, takeIsAllowed)?.let { illegalBecause(it) } ?: legal()
}

data class MoveLegality private constructor(val illegalReason: IllegalMoveReason?) {

    fun legal() = illegalReason == null
    companion object {
        fun legal() = MoveLegality(null)
        fun illegalBecause(reason: IllegalMoveReason) = MoveLegality(reason)
    }
}
enum class IllegalMoveReason {
    ILLEGAL_MOVE_SHAPE, OPPONENT_PIECE_IN_THE_WAY, FRIEND_PIECE_IN_THE_WAY
}
fun IllegalMoveReason?.asMoveLegality() = if (this != null) illegalBecause(this) else legal()

data class Rook(override val side: Side) : Piece {

    override fun canMove(board: Board, history: MoveHistory, move: Move): MoveLegality {
        if (
        // move along a file
        move.fileDistance() == 0 && move.rankDistance() > 0
        || // move along a rank
        move.rankDistance() == 0 && move.fileDistance() > 0
            ) {
            return checkPathClear(board, move)
        }
        return illegalBecause(ILLEGAL_MOVE_SHAPE)
    }
}

data class King(override val side: Side) : Piece {
    override fun canMove(board: Board, history: MoveHistory, move: Move): MoveLegality {
        if (max(move.rankDistance(), move.fileDistance()) == 1) {
            return checkPathClear(board, move)
        }

        val castle = asCastle(board, move) ?: return illegalBecause(ILLEGAL_MOVE_SHAPE)
        return if (canCastle(castle, history)) legal() else illegalBecause(ILLEGAL_MOVE_SHAPE)
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

        if (board.findObstacle(Move(move.from, expectedRookSquare).path().dropLast(1), side, takeAtFinalSquareAllowed = false) != null) {
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

data class Pawn(override val side: Side) : Piece {
    override fun canMove(board: Board, history: MoveHistory, move: Move): MoveLegality {
        // one square forward
        if (move.rankDistance() == 1
            && move.fileDistance() == 0
            && move.isForward(side)) {
            return checkPathClear(board, move, takeIsAllowed = false)
        }
        // two squares forward
        if ((side == Side.WHITE && move.from.rank == 2 && move.to.rank == 4
            || side == Side.BLACK && move.from.rank == 7 && move.to.rank == 5)
            && move.fileDistance() == 0) {
            return checkPathClear(board, move, takeIsAllowed = false)
        }

        // takes
        if (move.isForward(side) && move.rankDistance() == 1 && move.fileDistance() == 1) {
            val targetPiece = board.pieceAt(move.to)
            if (targetPiece != null) {
                // regular take
                return if (targetPiece.side == side) illegalBecause(FRIEND_PIECE_IN_THE_WAY) else legal()
            } else if (move.from.rank == (if (side == Side.WHITE) 5 else 4)) {
                // en-passant
                val enPassantTakenSquare = move.to.withRank(move.from.rank)
                return if (board.pieceAt(enPassantTakenSquare) == Pawn(side.opposite())
                    && history.lastMove()?.to == enPassantTakenSquare
                )
                    legal()
                else
                    illegalBecause(ILLEGAL_MOVE_SHAPE)
            }
        }

        return illegalBecause(ILLEGAL_MOVE_SHAPE)
    }

}

data class Knight(override val side: Side) : Piece {
    override fun canMove(board: Board, history: MoveHistory, move: Move): MoveLegality {
        if (move.rankDistance() * move.fileDistance() == 2) {
            val targetPiece = board.pieceAt(move.to)
            return if (targetPiece?.takeIf { it.side == this.side } == null) legal() else illegalBecause(FRIEND_PIECE_IN_THE_WAY)
        } else {
            return illegalBecause(ILLEGAL_MOVE_SHAPE)
        }
    }
}
