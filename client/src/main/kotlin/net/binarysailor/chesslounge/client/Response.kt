package net.binarysailor.chesslounge.client

import java.util.*


internal sealed class Response {

    abstract fun handle(handler: ChessLoungeClient.Handler)

    internal class SeekResponse(val seekId: UUID, val ok: Boolean, val message: String?) : Response() {
        override fun handle(handler: ChessLoungeClient.Handler) {
            handler.seekResponseReceived(SeekID(seekId))
        }
    }

    internal class GameStarted(val gameId: UUID, val white: Player, val black: Player, val seekId: UUID?) :
        Response() {
        override fun handle(handler: ChessLoungeClient.Handler) {
            handler.gameStarted(GameID(gameId), white, black, seekId?.let { SeekID(it) })
        }
    }

    internal class MoveResponse(val gameId: UUID, val moveSymbol: String, val error: Error?, val success: Success?) : Response() {
        override fun handle(handler: ChessLoungeClient.Handler) {
            val gmId = GameID(gameId)
            val move = Move(moveSymbol)
            success?.run {
                handler.playerMoved(gmId, move, Position(newPosition))
            }
            error?.run {
                handler.moveAttempted(gmId, move, code, message)
            }
        }

        data class Error(val code: String, val message: String?)
        data class Success(val number: Int, val newPosition: String)
    }
}
