package net.binarysailor.chesslounge.chesshouse

import net.binarysailor.chesslounge.chesshouse.api.WSPlayerMessaging
import net.binarysailor.chesslounge.chesshouse.api.authentication.TrivialAuthenticator
import net.binarysailor.chesslounge.chesshouse.api.runChessHouseApi
import net.binarysailor.chesslounge.chesshouse.model.*
import net.binarysailor.chesslounge.chesshouse.model.messaging.*
import net.binarysailor.chesslounge.chesshouse.model.messaging.MoveResponseMessage.MoveDetails
import net.binarysailor.chesslounge.engine.exception.IllegalMoveException

class ChessHouse(private val games: GameRepository) {
    private val connectedPlayers: MutableMap<PlayerID, Player> = mutableMapOf()

    fun games(): List<Game> = games.allGames()

    fun createGame(white: Player, black: Player): CreateGameResult {
        val game = Game(white, black)
        games.add(game)
        return CreateGameResult(
            game,
            sharedMessage(GameStartedMessage(game.id, white, black)) {
                toPlayers(connectedPlayers.values)
            }
        )
    }

    fun addPlayer(player: Player) {
        connectedPlayers[player.id] = player
    }

    fun removePlayer(player: Player) {
        connectedPlayers.remove(player.id)
    }

    fun playerPlaying(player: Player): Boolean {
        return games.allGames().any { it.currentlyPlayed && it.hasPlayer(player) }
    }

    fun makeMove(player: Player, gameID: GameID, moveSymbol: String): MakeMoveResult {
        data class ResponseAndAudience(val response: MoveResponseMessage, val audience: Collection<Player>) {
            fun asResult() = MakeMoveResult(sharedMessage(response) { toPlayers(audience) })
        }

        val responseAndAudience: ResponseAndAudience = moveSymbol.let {
            try {
                val game = game(gameID) ?: throw GameNotFound()
                game.mustBePlayerTurn(player)
                val moveNumber = game.move(it)
                ResponseAndAudience(
                    response = MoveResponseMessage(gameID, it, success = MoveDetails(moveNumber, FENPosition(game.board))),
                    audience = game.audience()
                )
            } catch (e: IllegalMoveException) {
                ResponseAndAudience(
                    response = MoveResponseMessage(gameID, it, error = Error("ILLEGAL", e.reason.toString())),
                    audience = listOf(player)
                )
            } catch (e: GameNotFound) {
                ResponseAndAudience(
                    response = MoveResponseMessage(gameID, it, error = Error("ILLEGAL", "GAME_NOT_FOUND")),
                    audience = listOf(player)
                )
            } catch (e: NotAPlayer) {
                ResponseAndAudience(
                    response = MoveResponseMessage(gameID, it, error = Error("ILLEGAL", "UNAUTHORISED")),
                    audience = listOf(player)
                )
            } catch (e: OpponentTurn) {
                ResponseAndAudience(
                    response = MoveResponseMessage(gameID, it, error = Error("ILLEGAL", "OPPONENT_TURN")),
                    audience = listOf(player)
                )
            }
        }

        return responseAndAudience.asResult()
    }

    fun findPlayer(playerName: String): Player? = connectedPlayers.values.find { it.name == playerName }

    private fun game(gameID: GameID): Game? = games().find { it.id == gameID }

    private fun Game.audience(): List<Player> = listOf(this.white, this.black)

    private fun Game.mustBePlayerTurn(player: Player) {
        val side = playerSide(player) ?: throw NotAPlayer()
        if (side != board.sideToMove) throw OpponentTurn()
    }

    data class CreateGameResult(val game: Game, val playerMessages: List<PlayerMessage>)
    data class MakeMoveResult(val playerMessages: List<PlayerMessage>)
}

data class ChessHouseConfiguration(val port: Int, val playerRepositoryProvider: () -> PlayerRepository)

fun main() {
    runChessHouse(ChessHouseConfiguration(
        port = 8122,
        playerRepositoryProvider = { InMemoryPlayerRepository() }
    ))
}

fun runChessHouse(config: ChessHouseConfiguration): ChessHouse {
    val gameRepository = GameRepository()
    val chessHouse = ChessHouse(gameRepository)
    val playerRepository = config.playerRepositoryProvider.invoke()
    val playerMessaging = WSPlayerMessaging()
    val gameMatcher = GameMatcher(chessHouse, playerMessaging)
    runChessHouseApi(config, TrivialAuthenticator(playerRepository), chessHouse, gameMatcher, playerMessaging)
    return chessHouse
}

open class ChessHouseException : Exception()
class GameNotFound : ChessHouseException()
class NotAPlayer : ChessHouseException()
class OpponentTurn : ChessHouseException()