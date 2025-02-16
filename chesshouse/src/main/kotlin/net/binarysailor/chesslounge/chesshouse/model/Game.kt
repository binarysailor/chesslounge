package net.binarysailor.chesslounge.chesshouse.model

import net.binarysailor.chesslounge.chesshouse.model.GameStatus.CURRENTLY_PLAYED
import net.binarysailor.chesslounge.engine.*
import net.binarysailor.chesslounge.engine.Side.BLACK
import net.binarysailor.chesslounge.engine.Side.WHITE
import java.util.*
import java.util.UUID.randomUUID

class Game(white: Player, black: Player) {

    val id: GameID = GameID(randomUUID())
    val board: Board = Board()
    private val players: Map<Side, Player>
    private var status: GameStatus = CURRENTLY_PLAYED

    val white: Player
        get() = this.players[WHITE]!!

    val black: Player
        get() = this.players[BLACK]!!

    val currentlyPlayed: Boolean
        get() = this.status == CURRENTLY_PLAYED

    init {
        players = mapOf(WHITE to white, BLACK to black)
    }
    fun move(moveSymbol: String) = board.execute(moveSymbol)

    fun hasPlayer(player: Player) = playerSide(player) != null

    fun playerSide(player: Player): Side? = if (white == player) WHITE else if (black == player) BLACK else null
}

@JvmInline
value class GameID(val id: UUID)

enum class GameStatus {
    CURRENTLY_PLAYED, FINISHED
}