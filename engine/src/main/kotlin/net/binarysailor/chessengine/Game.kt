package net.binarysailor.chessengine

enum class Side { WHITE, BLACK }
interface MoveHistory {
    fun hasMovesFrom(square: Square): Boolean
}