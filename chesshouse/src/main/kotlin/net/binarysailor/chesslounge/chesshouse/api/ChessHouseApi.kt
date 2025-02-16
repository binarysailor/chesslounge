package net.binarysailor.chesslounge.chesshouse.api

import net.binarysailor.chesslounge.chesshouse.ChessHouse
import net.binarysailor.chesslounge.chesshouse.ChessHouseConfiguration
import net.binarysailor.chesslounge.chesshouse.GameMatcher
import net.binarysailor.chesslounge.chesshouse.api.authentication.Authenticator
import net.binarysailor.chesslounge.chesshouse.model.Player
import spark.Request
import spark.Spark
import spark.Spark.*

fun runChessHouseApi(config: ChessHouseConfiguration, authenticator: Authenticator, chessHouse: ChessHouse, gameMatcher: GameMatcher, playerMessaging: WSPlayerMessaging) {
    val json = JsonTransformer()

    port(config.port)

    webSocket("/house", ChessHouseWSHandler(authenticator, chessHouse, gameMatcher, playerMessaging))

    before({ req, _ ->
        setPlayer(req, authenticator.authenticatePlayer(req))
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

fun net.binarysailor.chesslounge.chesshouse.model.Game.toApiResponse(): Game {
    return Game(this.id.id, this.white.name, this.black.name)
}

fun setPlayer(req: Request, player: Player) {
    req.attribute("_player", player)
}
