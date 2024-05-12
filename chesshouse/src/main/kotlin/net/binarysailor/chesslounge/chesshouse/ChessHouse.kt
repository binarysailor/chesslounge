package net.binarysailor.chesslounge.chesshouse

import net.binarysailor.chesslounge.chesshouse.api.buildChessHouseApi

class ChessHouse(private val games: GameRepository) {
    private val connectedPlayers: MutableMap<Player, PlayerChannel> = mutableMapOf()

    fun games(): List<Game> = games.allGames()

    fun createGame(white: Player, black: Player, messagesToSend: ((Player) -> InstantMessage)? = null): Game {
        val game = Game(white, black)
        games.add(game)
        messagesToSend?.let {msg ->
            connectedPlayers.filterKeys { it == white || it == black }.forEach { (player, channel) -> channel.send(msg.invoke(player)) }
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

enum class MessageType {
    SEEK_RESPONSE,
    GAME_STARTED
}

data class InstantMessage(val type: MessageType, val payload: Any)

fun main() {
    val gameRepository = GameRepository()
    val chessHouse = ChessHouse(gameRepository)
    val playerRepository = PlayerRepository()
    val gameMatcher = GameMatcher(chessHouse)
    buildChessHouseApi(chessHouse, playerRepository, gameMatcher)
}