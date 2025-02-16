package net.binarysailor.chesslounge.chesshouse

import net.binarysailor.chesslounge.chesshouse.model.Player
import net.binarysailor.chesslounge.chesshouse.model.messaging.PlayerMessaging
import net.binarysailor.chesslounge.chesshouse.model.messaging.SeekResponseMessage
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

class GameMatcher(private val chessHouse: ChessHouse, private val playerMessaging: PlayerMessaging) {
    private val seekers: ConcurrentHashMap<Player, SeekRequest> = ConcurrentHashMap()
    private val requests: BlockingQueue<SeekRequest> = LinkedBlockingQueue()
    private val log = LoggerFactory.getLogger(javaClass)

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

    fun removeSeeker(seeker: Player) {
        seekers.remove(seeker)
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
        log.debug("Trying to match players")
        if (seekers.size > 1) {
            val i = seekers.keys().iterator()
            val white = i.next()
            val black = i.next()
            seekers.remove(white)
            seekers.remove(black)

            val result = chessHouse.createGame(white, black)
            log.debug("Game {} created between {} and {}", result.game.id, result.game.white.name, result.game.black.name)
            playerMessaging.messagePlayers(result.playerMessages) { null }
        }
    }

    private fun respondFailure(request: SeekRequest, message: String) {
        playerMessaging.messagePlayer(request.player.id, SeekResponseMessage(request.id, false, message), null)
    }

    private fun respondSuccess(request: SeekRequest) {
        playerMessaging.messagePlayer(request.player.id, SeekResponseMessage(request.id, true), null)
    }

    data class SeekRequest(val player: Player, val id: SeekID = SeekID())

    @JvmInline
    value class SeekID(val id: UUID) {
        constructor() : this(UUID.randomUUID())
    }
}