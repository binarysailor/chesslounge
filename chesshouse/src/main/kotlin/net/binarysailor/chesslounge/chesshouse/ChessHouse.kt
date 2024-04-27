package net.binarysailor.chesslounge.chesshouse

import net.binarysailor.chesslounge.chesshouse.api.GamesResponse
import net.binarysailor.chesslounge.chesshouse.api.serializer.JsonTransformer
import spark.Spark.get
import spark.Spark.port

class ChessHouse {
    private val games: GameRepository = GameRepository()

    fun games(): List<Game> = games.allGames()

    fun createGame(white: Player, black: Player): Game {
        val game = Game(white, black)
        games.add(game)
        return game
    }
}

fun main() {
    val chessHouse = ChessHouse()
    val json = JsonTransformer()

    port(8122)

    get("/games", fun (req, res): GamesResponse {
        return GamesResponse(chessHouse.games().map { it.toApiResponse() })
    }, json)
}

fun Game.toApiResponse(): net.binarysailor.chesslounge.chesshouse.api.Game {
    return net.binarysailor.chesslounge.chesshouse.api.Game(this.id.id, this.white.name, this.black.name)
}