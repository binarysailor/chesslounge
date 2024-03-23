package net.binarysailor.chesslounge.chesshouse

class GameRepository {
    private val games: MutableMap<GameID, Game> = mutableMapOf()

    fun findGame(id: GameID): Game? = games[id]
}