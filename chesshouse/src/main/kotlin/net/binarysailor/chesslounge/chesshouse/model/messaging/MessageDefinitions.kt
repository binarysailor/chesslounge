package net.binarysailor.chesslounge.chesshouse.model.messaging

import net.binarysailor.chesslounge.chesshouse.GameMatcher.SeekID
import net.binarysailor.chesslounge.chesshouse.model.FENPosition
import net.binarysailor.chesslounge.chesshouse.model.GameID
import net.binarysailor.chesslounge.chesshouse.model.Player
import java.util.*

abstract class InstantMessage(val type: String)

class BadClientMessage : InstantMessage("BAD_CLIENT_MESSAGE")

data class SeekResponseMessage(val seekId: SeekID, val ok: Boolean, val message: String? = null)
    : InstantMessage("SEEK_RESPONSE")

data class GameStartedMessage(val gameId: GameID, val white: Player, val black: Player) : InstantMessage("GAME_STARTED")

data class MoveResponseMessage(
    val gameId: GameID,
    val symbol: String,
    val error: Error? = null,
    val success: MoveDetails? = null
) : InstantMessage("MOVE_RESPONSE") {
    data class MoveDetails(val number: Int, val newPosition: FENPosition)
}

@JvmInline
value class MessageId(val id: UUID)
data class Error(val code: String, val message: String)