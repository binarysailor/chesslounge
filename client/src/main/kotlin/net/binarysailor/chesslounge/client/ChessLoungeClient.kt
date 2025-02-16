package net.binarysailor.chesslounge.client

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import java.net.URL
import java.util.*
import kotlin.concurrent.thread

class ChessLoungeClient(private val serverUrl: URL, private val handler: Handler) {
    interface Handler {
        fun seekResponseReceived(seekId: SeekID)
        fun gameStarted(gameId: GameID, white: Player, black: Player, seekId: SeekID?)
        fun playerMoved(gameId: GameID, move: Move, newPosition: Position)
        fun moveAttempted(gameId: GameID, move: Move, code: String, message: String?)
    }

    constructor(serverUrl: String, handler: Handler) : this(URL(serverUrl), handler)

    private val client = HttpClient(CIO) {
        install(WebSockets)
    }
    private val objectMapper = ObjectMapper()

    private var webSocketSession: ClientWebSocketSession? = null
    private var receiverThread: Thread? = null

    init {
        objectMapper.registerModule(InstantMessageModule())
    }

    suspend fun connect(user: String) {

        coroutineScope {
            webSocketSession = client.webSocketSession(
                method = HttpMethod.Get,
                host = serverUrl.host,
                port = serverUrl.port,
                path = "/house"
            ) { header("Authorization", user) }
        }

        receiverThread = thread {
            runBlocking {
                while (true) {
                    try {
                        webSocketSession?.incoming?.receive()?.apply {
                            val message = objectMapper.readValue(data, Response::class.java)
                            message.handle(handler)
                        }
                    } catch (e: ClosedReceiveChannelException) {
                        break
                    }
                }
            }
        }
    }

    suspend fun disconnect() {
        webSocketSession?.close()
    }

    suspend fun seekPlay() {
        coroutineScope {
            val command = SeekPlayCommand()
            webSocketSession!!.send(objectMapper.writeValueAsString(command))
        }
    }

    suspend fun move(gameId: GameID, moveSymbol: String) {
        val command = MoveCommand(gameId, moveSymbol)
        webSocketSession!!.send(objectMapper.writeValueAsString(command))
    }

    class InstantMessageModule : SimpleModule() {
        init {
            addDeserializer(Response::class.java, InstantMessageDeserializer())
        }
    }

    internal class InstantMessageDeserializer : JsonDeserializer<Response>() {
        override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Response {
            val node = p!!.readValueAsTree<JsonNode>()
            val type = node["type"].textValue()
            return when (type) {
                "SEEK_RESPONSE" -> Response.SeekResponse(
                    UUID.fromString(node["seekId"].textValue()),
                    node["ok"].booleanValue(),
                    node["message"]?.asText()
                )

                "GAME_STARTED" -> Response.GameStarted(
                    UUID.fromString(node["gameId"].textValue()),
                    player(node["white"]),
                    player(node["black"]),
                    if (node.has("seekId")) UUID.fromString(node["seekId"].textValue()) else null
                )

                "MOVE_RESPONSE" -> Response.MoveResponse(
                    UUID.fromString(node["gameId"].textValue()),
                    node["symbol"].textValue(),
                    node["error"]?.let {
                        Response.MoveResponse.Error(it["code"].textValue(), it["message"].textValue())
                    },
                    node["success"]?.let {
                        Response.MoveResponse.Success(
                            it["number"].intValue(),
                            it["newPosition"].textValue()
                        )
                    }
                )
                else -> null
            } ?: throw IllegalStateException("Cannot deserialize message")
        }

        private fun player(node: JsonNode): Player =
            Player(UUID.fromString(node["id"].textValue()), node["name"].textValue())
    }
}