package net.binarysailor.chesslounge.chesshouse.api

import java.util.UUID

data class User(val id: UserID, val name: String)

@JvmInline
value class UserID(val id: UUID)