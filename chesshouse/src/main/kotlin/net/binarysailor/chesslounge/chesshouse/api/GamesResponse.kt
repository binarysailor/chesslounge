package net.binarysailor.chesslounge.chesshouse.api

import java.util.*

data class GamesResponse(val games: List<Game>)

data class Game(val id: UUID, val white: String, val black: String)