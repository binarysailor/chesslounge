package net.binarysailor.chesslounge.engine

import kotlin.math.ceil

internal class GameRecord : MoveHistory {

    private val moves : MutableList<Move> = mutableListOf()

    fun pieceMoved(move: Move) = moves.add(move)
    fun reset() = moves.clear()
    override fun hasMovesFrom(square: Square) = moves.any { it.from == square }

    override fun lastMove(): Move? = if (moves.size > 0) moves[moves.lastIndex] else null

    val lastMoveNumber: Int
        get() = ceil( moves.size.toDouble() / 2).toInt()
}