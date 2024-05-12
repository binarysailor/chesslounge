package net.binarysailor.chesslounge.chesshouse

import java.util.UUID.randomUUID

class PlayerRepository {
    private val players: MutableMap<PlayerID, Player> = mutableMapOf()

    init {
        listOf(
            Player(PlayerID(randomUUID()), "tomek"),
            Player(PlayerID(randomUUID()), "dominik"),
            Player(PlayerID(randomUUID()), "andrzej"),
            Player(PlayerID(randomUUID()), "wiktor"),
        ).forEach {
            players.put(it.id, it)
        }
    }

    fun findPlayer(id: PlayerID): Player? = players[id]
    fun findPlayerByName(name: String): Player? = players.values.find { it.name == name }
    fun allPlayers(): List<Player> = players.values.toList()
    fun add(player: Player): Unit {
        players[player.id] = player
    }
}