package net.binarysailor.chesslounge.chesshouse.messages

import net.binarysailor.chesslounge.chesshouse.GameID
import net.binarysailor.chesslounge.chesshouse.GameMatcher
import net.binarysailor.chesslounge.chesshouse.PlayerID

open class InstantMessage(val type: String)

class SeekResponseMessage(val seekId: GameMatcher.SeekID, val ok: Boolean, val message: String? = null)
    : InstantMessage("SEEK_RESPONSE")

class GameStartedMessage(val seekId: GameMatcher.SeekID, val gameId: GameID, val white: Player, val black: Player)
    : InstantMessage("GAME_STARTED") {
        constructor(seekId: GameMatcher.SeekID,
                    gameId: GameID,
                    white: net.binarysailor.chesslounge.chesshouse.Player,
                    black: net.binarysailor.chesslounge.chesshouse.Player)
                : this(seekId = seekId, gameId = gameId, white = Player(white.id, white.name), black = Player(black.id, black.name))
    }

data class Player(val id: PlayerID, val name: String)