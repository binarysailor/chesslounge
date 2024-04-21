package net.binarysailor.chesslounge.chesshouse

import net.binarysailor.chesslounge.engine.*
import net.binarysailor.chesslounge.engine.Side.BLACK
import net.binarysailor.chesslounge.engine.Side.WHITE

class Game(white: Player, black: Player) {

    private val board: Board = Board()
    private val players: Map<Side, Player>

    init {
        players = mapOf(WHITE to white, BLACK to black)
    }
    fun move(moveSymbol: String) {
        board.execute(moveSymbol)
    }
}

