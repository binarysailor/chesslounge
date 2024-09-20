package net.binarysailor.chesslounge.chesshouse

import net.binarysailor.chesslounge.chesshouse.messages.GameStartedMessage
import net.binarysailor.chesslounge.chesshouse.messages.SeekResponseMessage
import java.util.*
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

    fun addSeeker(seeker: Player): SeekID {
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
            chessHouse.createGame(white, black) { player, game ->
                GameStartedMessage(if (player == white) whiteRequest!!.id else blackRequest!!.id, game.id, white, black)
            }
        }
    }

    private fun respondFailure(request: SeekRequest, message: String) {
        chessHouse.messagePlayer(request.player, SeekResponseMessage(request.id, false, message))
    }

    private fun respondSuccess(request: SeekRequest) {
        chessHouse.messagePlayer(request.player, SeekResponseMessage(request.id, true))
    }

    data class SeekRequest(val player: Player, val id: SeekID = SeekID())
    @JvmInline
    value class SeekID(val id: UUID) {
        constructor() : this(UUID.randomUUID())
    }
}