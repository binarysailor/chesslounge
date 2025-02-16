package net.binarysailor.chesslounge.chesshouse.model.messaging

import net.binarysailor.chesslounge.chesshouse.model.Player

data class PlayerMessage(val player: Player, val message: InstantMessage)


internal class PlayerMessagesBuilder {
    private var recipients: Collection<Player> = listOf()
    private var recipientMessageSupplier: ((Player) -> InstantMessage)? = null
    fun toPlayers(players: Collection<Player>) {
        recipients = players
    }

    fun supplyingMessage(supplier: (Player) -> InstantMessage) {
        recipientMessageSupplier = supplier
    }

    fun build() = recipients.map {
        PlayerMessage(
            it,
            recipientMessageSupplier!!.invoke(it)
        )
    }
}

internal fun sharedMessage(message: InstantMessage, init: PlayerMessagesBuilder.() -> Unit): List<PlayerMessage> {
    val builder = PlayerMessagesBuilder()
    builder.supplyingMessage { message }
    builder.init()
    return builder.build()
}
