package net.binarysailor.chesslounge.chesshouse

class GameRepository {
    private val games: MutableMap<GameID, Game> = mutableMapOf()

    fun findGame(id: GameID): Game? = games[id]
    fun allGames(): List<Game> = games.values.toList()
    fun add(game: Game): Unit {
        games[game.id] = game
    }
}