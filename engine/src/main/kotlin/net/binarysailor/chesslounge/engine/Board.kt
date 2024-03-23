package net.binarysailor.chesslounge.engine

import net.binarysailor.chesslounge.engine.exception.IllegalMoveException
import java.text.ParseException

class Board {
    private var sideToMove: Side = Side.WHITE
    private val squares: Array<Array<Piece?>> = Array(8) {
        Array(8) { null }
    }
    private val gameRecord = GameRecord()


    fun execute(moveSymbol: String) {
        val move = Move.parse(moveSymbol)
        val piece = pieceAt(move.from) ?: throw IllegalMoveException(IllegalMoveReason.NO_PIECE_FOUND)
        val response = piece.tryMove(this, gameRecord, move)
        if (!response.legality.legal) {
            throw IllegalMoveException(response.legality.illegalReason!!)
        }

        move.execute(this)
        response.sideEffects.forEach { it.execute(this) }
        gameRecord.pieceMoved(move)
    }

    internal fun pieceAt(square: Square): Piece? =
        squares[square.rank - 1][square.file - 1]

    internal fun addPiece(square: Square, piece: Piece) {
        squares[square.rank - 1][square.file - 1] = piece
    }

    internal fun removePiece(square: Square) {
        squares[square.rank - 1][square.file - 1] = null
    }

    internal fun findObstacle(path: List<Square>, movingSide: Side, takeAtFinalSquareAllowed: Boolean): IllegalMoveReason? {
        if (path.size < 2) {
            return null
        }

        var pieceInTheWay = path.drop(1).dropLast(1).map { pieceAt(it) }.find { it != null }
        if (pieceInTheWay == null) {
            pieceInTheWay =
                if (takeAtFinalSquareAllowed)
                    pieceAt(path.last())?.takeIf { it.side == movingSide }
                else
                    pieceAt(path.last())
        }

        if (pieceInTheWay != null) {
            return if (pieceInTheWay.side == movingSide)
                IllegalMoveReason.FRIEND_PIECE_IN_THE_WAY
            else
                IllegalMoveReason.OPPONENT_PIECE_IN_THE_WAY
        }

        return null
    }

}

internal data class Square(val file: Int, val rank: Int) {

    fun name(): String = FILES[file - 1] + rank.toString()

    override fun toString() = name()

    fun withFile(fileName: Char): Square = withFile(fileNameToIndex(fileName))
    fun withFile(file: Int) = Square(file, rank)
    fun withRank(rank: Int) = Square(file, rank)

    companion object {
        private const val FILES = "abcdefgh"

        private fun fileNameToIndex(fileName: Char): Int {
            if (fileName.lowercase() !in FILES) {
                throw ParseException("Could not parse file $fileName", 0)
            }
            return FILES.indexOf(fileName.lowercase()) + 1
        }

        private fun rankNameToIndex(rankName: Char): Int {
            if (!rankName.isDigit()) {
                throw ParseException("Could not parse rank $rankName", 1)
            }
            val rank = rankName.digitToInt()
            if (rank < 1 || rank > 8) {
                throw ParseException("Could not parse rank $rank", 1)
            }
            return rank
        }

        fun of(name: String): Square {
            if (name.length < 1) {
                throw ParseException("Could not parse file", 0)
            }
            if (name.length < 2) {
                throw ParseException("Could not parse rank", 1)
            }

            val fileName = name.lowercase()[0]
            val file = fileNameToIndex(fileName)

            val rankName = name[1]
            val rank = rankNameToIndex(rankName)

            return Square(file, rank)
        }
    }
}
internal fun square(name: String) = Square.of(name)
