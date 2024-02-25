package net.binarysailor.chesslounge.engine

enum class Side {
                WHITE, BLACK;
    fun opposite() = when (this) {
        BLACK -> WHITE
        WHITE -> BLACK
    }
}
interface MoveHistory {
    fun hasMovesFrom(square: Square): Boolean
    abstract fun lastMove(): Move?
}