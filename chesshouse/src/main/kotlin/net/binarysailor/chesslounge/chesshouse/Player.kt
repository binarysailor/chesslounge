package net.binarysailor.chesslounge.chesshouse

import java.util.UUID

data class Player(val id: PlayerID, val name: String)

@JvmInline
value class PlayerID(val id: UUID) {
    override fun toString() = id.toString()
}