package net.binarysailor.chesslounge.chesshouse.model

import net.binarysailor.chesslounge.engine.Board

@JvmInline
value class FENPosition(private val fen: String) {

    constructor(board: Board) : this(convert(board))

    override fun toString() = fen
}

fun convert(board: Board) = "TODO"