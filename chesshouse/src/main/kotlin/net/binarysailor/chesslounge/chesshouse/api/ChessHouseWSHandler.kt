package net.binarysailor.chesslounge.chesshouse.api

import com.google.gson.Gson
import net.binarysailor.chesslounge.chesshouse.*
import net.binarysailor.chesslounge.chesshouse.api.authentication.Authenticator
import net.binarysailor.chesslounge.chesshouse.model.GameID
import net.binarysailor.chesslounge.chesshouse.model.Player
import net.binarysailor.chesslounge.chesshouse.model.messaging.BadClientMessage
import net.binarysailor.chesslounge.chesshouse.model.messaging.Error
import net.binarysailor.chesslounge.chesshouse.model.messaging.MessageId
import net.binarysailor.chesslounge.chesshouse.model.messaging.MoveResponseMessage
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage
import org.eclipse.jetty.websocket.api.annotations.WebSocket
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@WebSocket
internal class ChessHouseWSHandler(
    private val authenticator: Authenticator,
    private val chessHouse: ChessHouse,
    private val gameMatcher: GameMatcher,
    private val playerMessaging: WSPlayerMessaging) {

    private val playersBySession = ConcurrentHashMap<Session, Player>()
    private val gson = Gson()
    private val log = LoggerFactory.getLogger(javaClass)

    @OnWebSocketConnect
    fun onConnect(session: Session) {
        val player = session.player()
        chessHouse.addPlayer(player)
        playerMessaging.addPlayer(player, session)
        log.info("Player ${player.name} connected")
    }

    @OnWebSocketMessage
    fun onMessage(session: Session, message: String) {
        val clientMessage = gson.fromJson(message, ClientMessage::class.java)
        log.debug("Message from {}: {}", playersBySession[session], message)
        try {
            handleClientMessage(session, clientMessage)
        } catch (e: WSHandlerException) {
            session.remote.sendString("""
                {
                    "correlationId": "${clientMessage.messageId}",
                    "message": "error",
                    "details": "${e.message}"
                }
                """.trimIndent()
            )
        }
    }

    private fun handleClientMessage(session: Session, clientMessage: ClientMessage) {
        val player = playersBySession[session] ?: throw WSHandlerException("SESSION_UNKNOWN")

        when {
            clientMessage.message == "seekplay" -> gameMatcher.addSeeker(player)
            clientMessage.message.startsWith("game") -> {
                val parts = clientMessage.message.split(" ")
                if (parts.size < 3) {
                    playerMessaging.messagePlayer(player.id, BadClientMessage(), clientMessage.messageId)
                    return
                }

                val gameId = GameID(UUID.fromString(parts[1]))
                val command = parts[2]

                if (command == "move") {
                    val moveSymbol = parts[3]
                    log.debug("Received move {} in game {} from {}", moveSymbol, gameId, player.name)
                    val makeMoveResult = chessHouse.makeMove(player, gameId, moveSymbol)
                    log.debug("Result of the move attempt: {}", makeMoveResult)
                    playerMessaging.messagePlayers(makeMoveResult.playerMessages) {
                        if (it.id == player.id) clientMessage.messageId else null
                    }
                }
            }
        }
    }

    @OnWebSocketClose
    fun onClose(userSession: Session, statusCode: Int, reason: String?) {
        val player = userSession.player()
        log.debug("Player {} ({}) disconnected", player.name, player.id)
        gameMatcher.removeSeeker(player)
        chessHouse.removePlayer(player)
        playerMessaging.removePlayer(player)
    }

    private fun Session.player(): Player {
        return playersBySession[this] ?: run {
            authenticator.authenticatePlayer(upgradeRequest).also { playersBySession[this] = it }
        }
    }

    internal data class ClientMessage(val messageId: MessageId, val message: String)

    internal class WSHandlerException(override val message: String?) : Exception(message)
}

