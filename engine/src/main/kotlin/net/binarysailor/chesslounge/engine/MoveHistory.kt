package net.binarysailor.chesslounge.engine

internal interface MoveHistory {
    fun hasMovesFrom(square: Square): Boolean
    abstract fun lastMove(): Move?
}