package net.binarysailor.chesslounge.engine

import net.binarysailor.chesslounge.engine.exception.MoveParseException
import kotlin.math.abs
import kotlin.math.max

internal interface BoardModification {
    fun execute(board: Board)
}

internal data class Move(val from: Square, val to: Square) : BoardModification {

    constructor(fromName: String, toName: String): this(square(fromName), square(toName))

    val rankDistance = abs(from.rank - to.rank)
    val fileDistance = abs(from.file - to.file)

    fun isForward(side: Side) = if (side == Side.WHITE) to.rank > from.rank else to.rank < from.rank

    fun path(): List<Square> {
        val fileDelta = to.file - from.file
        val rankDelta = to.rank - from.rank
        val stepCount = max(abs(fileDelta), abs(rankDelta))

        return 0.rangeTo(stepCount).map {
            val rank = from.rank + it * rankDelta / stepCount
            val file = from.file + it * fileDelta / stepCount
            Square(file, rank)
        }.toList()
    }

    override fun execute(board: Board) {
        val piece = board.pieceAt(from)!!
        board.removePiece(from)
        board.removePiece(to)
        board.addPiece(to, piece)
    }

    override fun toString() = "$from-$to"

    companion object {
        internal fun parse(moveSymbol: String): Move {
            val stripped = moveSymbol.replace("-", "")
            if (stripped.length != 4) {
                throw MoveParseException(moveSymbol)
            }
            return Move(stripped.substring(0, 2), stripped.substring(2, 4))
        }
    }
}

internal data class Removal(val square: Square): BoardModification {
    override fun execute(board: Board) {
        board.removePiece(square)
    }
}
internal fun removal(squareName: String) = Removal(square(squareName))
