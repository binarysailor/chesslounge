package net.binarysailor.chesslounge.engine

import net.binarysailor.chesslounge.engine.IllegalMoveReason.FRIEND_PIECE_IN_THE_WAY
import net.binarysailor.chesslounge.engine.IllegalMoveReason.ILLEGAL_MOVE_SHAPE
import net.binarysailor.chesslounge.engine.MoveResponse.Companion.illegalBecause
import net.binarysailor.chesslounge.engine.MoveResponse.Companion.legalNoSideEffects
import net.binarysailor.chesslounge.engine.MoveResponse.Companion.legalWithSideEffects
import kotlin.math.max

internal abstract class Piece {
    abstract val side: Side

    internal abstract fun tryMove(board: Board, history: MoveHistory, move: Move): MoveResponse

    protected fun simpleMoveIfPathClear(board: Board, move: Move, takeIsAllowed: Boolean = true): MoveResponse =
        board.findObstacle(move.path(), side, takeIsAllowed)?.let { illegalBecause(it) } ?: legalNoSideEffects()

    override fun toString() = "%s %s".format(side, javaClass.simpleName)
    override fun equals(other: Any?) = other?.javaClass == this.javaClass && ((other as Piece).side == this.side)
}

internal class Rook(override val side: Side) : Piece() {

    override fun tryMove(board: Board, history: MoveHistory, move: Move): MoveResponse {
        if (
        // move along a file
        move.fileDistance == 0 && move.rankDistance > 0
        || // move along a rank
        move.rankDistance == 0 && move.fileDistance > 0
            ) {
            return simpleMoveIfPathClear(board, move)
        }
        return illegalBecause(ILLEGAL_MOVE_SHAPE)
    }
}

internal class King(override val side: Side) : Piece() {
    override fun tryMove(board: Board, history: MoveHistory, move: Move): MoveResponse {
        if (max(move.rankDistance, move.fileDistance) == 1) {
            return simpleMoveIfPathClear(board, move)
        }

        val castle = asCastle(board, move) ?: return illegalBecause(ILLEGAL_MOVE_SHAPE)
        return if (canCastle(castle, history)) legalWithSideEffects(listOf(castlingRookMove(castle))) else illegalBecause(ILLEGAL_MOVE_SHAPE)
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

    private fun castlingRookMove(castle: Castle): Move {
        val rookRank = startingSquare(castle.side).rank
        val rookTargetFile = if (castle.rookFile == 1) 4 else 6
        return Move(Square(castle.rookFile, rookRank), Square(rookTargetFile, rookRank))
    }

    private data class Castle(val side: Side, val rookFile: Int) {
        fun rookFrom() = startingSquare(side).withFile(rookFile)
    }


    companion object {
        fun startingSquare(side: Side) = square(if (side == Side.WHITE) "E1" else "E8")
    }
}

internal class Pawn(override val side: Side) : Piece() {
    override fun tryMove(board: Board, history: MoveHistory, move: Move): MoveResponse {
        // one square forward
        if (move.rankDistance == 1
            && move.fileDistance == 0
            && move.isForward(side)) {
            return simpleMoveIfPathClear(board, move, takeIsAllowed = false)
        }
        // two squares forward
        if ((side == Side.WHITE && move.from.rank == 2 && move.to.rank == 4
            || side == Side.BLACK && move.from.rank == 7 && move.to.rank == 5)
            && move.fileDistance == 0) {
            return simpleMoveIfPathClear(board, move, takeIsAllowed = false)
        }

        // takes
        if (move.isForward(side) && move.rankDistance == 1 && move.fileDistance == 1) {
            val targetPiece = board.pieceAt(move.to)
            if (targetPiece != null) {
                // regular take
                return if (targetPiece.side == side) illegalBecause(FRIEND_PIECE_IN_THE_WAY) else legalNoSideEffects()
            } else if (move.from.rank == (if (side == Side.WHITE) 5 else 4)) {
                // en-passant
                val enPassantTakenSquare = move.to.withRank(move.from.rank)
                return if (board.pieceAt(enPassantTakenSquare) == Pawn(side.opposite())
                    && history.lastMove()?.to == enPassantTakenSquare
                )
                    legalWithSideEffects(listOf( enPassantKill(enPassantTakenSquare)))
                else
                    illegalBecause(ILLEGAL_MOVE_SHAPE)
            }
        }

        return illegalBecause(ILLEGAL_MOVE_SHAPE)
    }

    private fun enPassantKill(enPassantTakenSquare: Square) = Removal(enPassantTakenSquare)
}

internal class Knight(override val side: Side) : Piece() {
    override fun tryMove(board: Board, history: MoveHistory, move: Move): MoveResponse {
        if (move.rankDistance * move.fileDistance == 2) {
            val targetPiece = board.pieceAt(move.to)
            return if (targetPiece?.takeIf { it.side == this.side } == null) legalNoSideEffects() else illegalBecause(FRIEND_PIECE_IN_THE_WAY)
        } else {
            return illegalBecause(ILLEGAL_MOVE_SHAPE)
        }
    }
}

internal class Bishop(override val side: Side) : Piece() {
    override fun tryMove(board: Board, history: MoveHistory, move: Move): MoveResponse {
        if (move.rankDistance == move.fileDistance) {
            return simpleMoveIfPathClear(board, move)
        } else {
            return illegalBecause(ILLEGAL_MOVE_SHAPE)
        }
    }
}