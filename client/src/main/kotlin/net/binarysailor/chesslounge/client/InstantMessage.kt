package net.binarysailor.chesslounge.client

import java.util.*


internal sealed class InstantMessage {

    abstract fun handle(handler: ChessLoungeClient.Handler)

    internal class SeekResponse(val seekId: UUID, val ok: Boolean, val message: String?) : InstantMessage() {
        override fun handle(handler: ChessLoungeClient.Handler) {
            handler.seekResponseReceived(SeekID(seekId))
        }
    }

    internal class GameStarted(val seekId: UUID, val gameId: UUID, val white: Player, val black: Player) :
        InstantMessage() {
        override fun handle(handler: ChessLoungeClient.Handler) {
            handler.gameStarted(SeekID(seekId), GameID(gameId))
        }
    }
}
