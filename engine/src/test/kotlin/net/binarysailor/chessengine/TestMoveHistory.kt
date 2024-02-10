package net.binarysailor.chessengine

class TestMoveHistory : MoveHistory {
    override fun hasMovesFrom(square: Square) = false
}