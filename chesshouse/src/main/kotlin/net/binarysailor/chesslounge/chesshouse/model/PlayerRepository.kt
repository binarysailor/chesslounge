package net.binarysailor.chesslounge.chesshouse.model

import java.util.UUID.randomUUID

interface PlayerRepository {
    fun findPlayer(id: PlayerID): Player?
    fun findPlayerByName(name: String): Player?
    fun allPlayers(): List<Player>
}

class InMemoryPlayerRepository : PlayerRepository {
    private val players: MutableMap<PlayerID, Player> = mutableMapOf()

    override fun findPlayer(id: PlayerID): Player? = players[id]
    override fun findPlayerByName(name: String): Player? = players.values.find { it.name == name }
    override fun allPlayers(): List<Player> = players.values.toList()
    fun add(player: Player) {
        players[player.id] = player
    }

    fun add(playerName: String) {
        add(Player(PlayerID(randomUUID()), playerName))
    }
}