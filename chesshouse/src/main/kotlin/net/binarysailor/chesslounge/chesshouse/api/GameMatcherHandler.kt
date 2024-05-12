package net.binarysailor.chesslounge.chesshouse.api

import com.google.gson.Gson
import net.binarysailor.chesslounge.chesshouse.ChessHouse
import net.binarysailor.chesslounge.chesshouse.GameMatcher
import net.binarysailor.chesslounge.chesshouse.InstantMessage
import net.binarysailor.chesslounge.chesshouse.Player
import net.binarysailor.chesslounge.chesshouse.PlayerChannel
import net.binarysailor.chesslounge.chesshouse.PlayerRepository
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage
import org.eclipse.jetty.websocket.api.annotations.WebSocket

@WebSocket
class GameMatcherHandler(private val playerRepository: PlayerRepository, private val chessHouse: ChessHouse, private val gameMatcher: GameMatcher) {

    @OnWebSocketConnect
    fun onConnect(user: Session) {
        val player = playerFromSession(user)
        println("${player.name} (${player.id}) connected")
        chessHouse.addPlayer(player, WebSocketPlayerChannel(user))
    }

    @OnWebSocketMessage
    fun onMessage(session: Session, message: String) {
        if ("seekplay" == message) {
            val player = playerFromSession(session)
            gameMatcher.addSeeker(player)
        }
    }

    @OnWebSocketClose
    fun onClose(user: Session, statusCode: Int, reason: String) {
        val player = playerFromSession(user)
        println("User Session closed: ${player.name} (${player.id})")
        chessHouse.removePlayer(player)
    }

    private fun playerFromSession(user: Session): Player {
        val authHeader = user.upgradeRequest.getHeader("Authorization") ?: throw UnauthorisedAccess()
        return playerRepository.findPlayerByName(authHeader) ?: throw UnauthorisedAccess()
    }
}

internal data class WebSocketPlayerChannel(private val session: Session): PlayerChannel {
    companion object {
        private val gson = Gson()
    }

    override fun send(message: InstantMessage) {
        session.remote.sendString(message.asJson())
    }

    private fun InstantMessage.asJson() = gson.toJson(this)
}