package net.binarysailor.chesslounge.chesshouse

import net.binarysailor.chesslounge.engine.*
import net.binarysailor.chesslounge.engine.Side.BLACK
import net.binarysailor.chesslounge.engine.Side.WHITE
import java.util.*
import java.util.UUID.randomUUID

class Game(white: Player, black: Player) {

    val id: GameID = GameID(randomUUID())
    private val board: Board = Board()
    private val players: Map<Side, Player>

    val white: Player
        get() = this.players[WHITE]!!

    val black: Player
        get() = this.players[BLACK]!!


    init {
        players = mapOf(WHITE to white, BLACK to black)
    }
    fun move(moveSymbol: String) {
        board.execute(moveSymbol)
    }
}

@JvmInline
value class GameID(val id: UUID)