package net.binarysailor.chesslounge.chesshouse

import net.binarysailor.chesslounge.chesshouse.api.runChessHouseApi
import net.binarysailor.chesslounge.chesshouse.messages.InstantMessage

class ChessHouse(private val games: GameRepository) {
    private val connectedPlayers: MutableMap<Player, PlayerChannel> = mutableMapOf()

    fun games(): List<Game> = games.allGames()

    fun createGame(white: Player, black: Player, messagesToSend: ((Player, Game) -> InstantMessage)? = null): Game {
        val game = Game(white, black)
        games.add(game)
        messagesToSend?.let {msg ->
            connectedPlayers.filterKeys { it == white || it == black }.forEach { (player, channel) -> channel.send(msg.invoke(player, game)) }
        }
        return game
    }

    fun addPlayer(player: Player, channel: PlayerChannel) {
        connectedPlayers[player] = channel
    }

    fun removePlayer(player: Player) {
        connectedPlayers.remove(player)
    }

    fun playerPlaying(player: Player): Boolean {
        return games.allGames().any { it.currentlyPlayed && it.hasPlayer(player) }
    }

    fun messagePlayer(target: Player, message: InstantMessage) {
        (connectedPlayers[target] ?: throw TargetUnreachable("$target not connected")).send(message)
    }
}

interface PlayerChannel {
    fun send(message: InstantMessage)
}

class TargetUnreachable(msg: String) : kotlin.NoSuchElementException(msg)

data class ChessHouseConfiguration(val port: Int)

fun main() {
    runChessHouse()
}

fun runChessHouse(config: ChessHouseConfiguration = ChessHouseConfiguration(8122)) {
    val gameRepository = GameRepository()
    val chessHouse = ChessHouse(gameRepository)
    val playerRepository = PlayerRepository()
    val gameMatcher = GameMatcher(chessHouse)
    runChessHouseApi(config, chessHouse, playerRepository, gameMatcher)
}