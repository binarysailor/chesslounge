package net.binarysailor.chesslounge.engine

enum class Side {
                WHITE, BLACK;
    fun opposite() = when (this) {
        BLACK -> WHITE
        WHITE -> BLACK
    }
}