package net.binarysailor.chessengine

import net.binarysailor.chesslounge.engine.Move
import net.binarysailor.chesslounge.engine.MoveHistory
import net.binarysailor.chesslounge.engine.Square

class TestMoveHistory : MoveHistory {

    private var lastMove: Move? = null
    private var movesFrom: MutableSet<Square> = mutableSetOf()

    override fun hasMovesFrom(square: Square) = movesFrom.contains(square)
    override fun lastMove(): Move? = lastMove

    fun withMoveFrom(square: Square) {
        movesFrom.add(square)
    }

    fun withLastMove(move: Move?) {
        lastMove = move
    }
}

fun moveHistory(init: TestMoveHistory.() -> Unit): TestMoveHistory {
    val history = TestMoveHistory()
    history.init()
    return history
}