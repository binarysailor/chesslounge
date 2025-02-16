package net.binarysailor.chesslounge.client

import java.util.UUID

@JvmInline
value class MessageID(val id: UUID)

@JvmInline
value class SeekID(val id: UUID)

@JvmInline
value class GameID(val id: UUID)