package net.binarysailor.chesslounge.chesshouse.api

import net.binarysailor.chesslounge.chesshouse.*
import spark.Request
import spark.Spark
import spark.Spark.before
import spark.Spark.get
import spark.Spark.path
import spark.Spark.port
import spark.Spark.webSocket

fun runChessHouseApi(config: ChessHouseConfiguration, chessHouse: ChessHouse, playerRepository: PlayerRepository, gameMatcher: GameMatcher) {
    val json = JsonTransformer()

    port(config.port)

    webSocket("/game-matcher", GameMatcherHandler(playerRepository, chessHouse, gameMatcher))

    before({ req, res ->
        val authHeader = req.headers("Authorization") ?: throw UnauthorisedAccess()
        setPlayer(req, playerRepository.findPlayerByName(authHeader) ?: throw UnauthorisedAccess())
    })


    path("/games") {
        get("", fun(req, res): GamesResponse {
            val games = chessHouse.games()
            return GamesResponse(games.map { it.toApiResponse() })
        }, json)
    }
}

fun stopChessHouseApi() {
    Spark.stop()
}

fun net.binarysailor.chesslounge.chesshouse.Game.toApiResponse(): Game {
    return Game(this.id.id, this.white.name, this.black.name)
}

fun setPlayer(req: Request, player: Player) {
    req.attribute("_player", player)
}

fun user(req: Request): Player = req.attribute("_player")

class UnauthorisedAccess : IllegalArgumentException()