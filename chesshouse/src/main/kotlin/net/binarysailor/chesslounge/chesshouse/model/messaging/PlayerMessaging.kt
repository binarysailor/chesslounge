package net.binarysailor.chesslounge.chesshouse.model.messaging

import net.binarysailor.chesslounge.chesshouse.model.Player
import net.binarysailor.chesslounge.chesshouse.model.PlayerID

interface PlayerMessaging {
    fun messagePlayer(playerId: PlayerID, message: InstantMessage, correlationId: MessageId?)
    fun messagePlayers(playerMessages: Collection<PlayerMessage>, correlationIdSupplier: (Player) -> MessageId?) {
        playerMessages.forEach { messagePlayer(it.player.id, it.message, correlationIdSupplier.invoke(it.player)) }
    }
}
