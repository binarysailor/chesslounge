package net.binarysailor.chesslounge.chesshouse.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import net.binarysailor.chesslounge.chesshouse.model.Player
import net.binarysailor.chesslounge.chesshouse.model.PlayerID
import net.binarysailor.chesslounge.chesshouse.model.messaging.InstantMessage
import net.binarysailor.chesslounge.chesshouse.model.messaging.MessageId
import net.binarysailor.chesslounge.chesshouse.model.messaging.PlayerMessaging
import org.eclipse.jetty.websocket.api.Session

class WSPlayerMessaging : PlayerMessaging {

    private val sessions: MutableMap<PlayerID, Session> = mutableMapOf()
    private val gson = Gson()

    override fun messagePlayer(playerId: PlayerID, message: InstantMessage, correlationId: MessageId?) {
        val messageNode = gson.toJsonTree(message) as JsonObject
        correlationId?.let { messageNode.addProperty("correlationId", it.id.toString()) }
        sessions[playerId]?.remote?.sendString(gson.toJson(messageNode))
    }

    fun addPlayer(player: Player, session: Session) {
        sessions[player.id] = session
    }

    fun removePlayer(player: Player) {
        sessions.remove(player.id)
    }

    internal fun hasPlayer(player: Player) = sessions.containsKey(player.id)
}