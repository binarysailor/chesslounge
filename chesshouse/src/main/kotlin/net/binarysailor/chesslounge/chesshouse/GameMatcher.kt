package net.binarysailor.chesslounge.chesshouse

import net.binarysailor.chesslounge.chesshouse.MessageType.GAME_STARTED
import net.binarysailor.chesslounge.chesshouse.MessageType.SEEK_RESPONSE
import org.eclipse.jetty.util.ConcurrentHashSet
import java.util.UUID
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

class GameMatcher(private val chessHouse: ChessHouse) {
    private val seekers: ConcurrentHashMap<Player, SeekRequest> = ConcurrentHashMap()
    private val requests: BlockingQueue<SeekRequest> = LinkedBlockingQueue()

    init {
        thread(name = "game-matcher") {
            while (true) {
                val request = requests.take()
                processSeekRequest(request)
            }
        }
    }

    fun addSeeker(seeker: Player): SeekRequestID {
        val request = SeekRequest(seeker)
        requests.put(request)
        return request.id
    }

    private fun processSeekRequest(request: SeekRequest) {
        if (seekers.containsKey(request.player)) {
            respondFailure(request, "Player already seeking a game")
        }
        if (chessHouse.playerPlaying(request.player)) {
            respondFailure(request, "Player already playing a game")
        }
        seekers.put(request.player, request)
        respondSuccess(request)
        tryMatching()
    }

    private fun tryMatching() {
        if (seekers.size > 1) {
            val i = seekers.keys().iterator()
            val white = i.next()
            val black = i.next()
            val whiteRequest = seekers.remove(white)
            val blackRequest = seekers.remove(black)
            chessHouse.createGame(white, black) { player ->
                InstantMessage(
                    GAME_STARTED,
                    mapOf(
                        "seekRequestId" to (if (player == white) whiteRequest!!.id.id else blackRequest!!.id.id),
                        "white" to mapOf("name" to white.name, "id" to white.id.id),
                        "black" to mapOf("name" to black.name, "id" to black.id.id)
                    )
                )
            }
        }
    }

    private fun respondFailure(request: SeekRequest, message: String) {
        chessHouse.messagePlayer(request.player, InstantMessage(
            SEEK_RESPONSE, SeekResponse(request.id, false, message)))
    }

    private fun respondSuccess(request: SeekRequest) {
        chessHouse.messagePlayer(request.player, InstantMessage(
            SEEK_RESPONSE, SeekResponse(request.id, true)))
    }

    data class SeekRequest(val player: Player, val id: SeekRequestID = SeekRequestID())
    data class SeekResponse(val id: SeekRequestID, val ok: Boolean, val message: String? = null)
    @JvmInline
    value class SeekRequestID(val id: UUID) {
        constructor() : this(UUID.randomUUID())
    }
}