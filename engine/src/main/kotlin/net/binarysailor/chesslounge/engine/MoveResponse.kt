package net.binarysailor.chesslounge.engine

import net.binarysailor.chesslounge.engine.exception.IllegalMoveException
import net.binarysailor.chesslounge.engine.MoveLegality.Companion.legal

internal data class MoveResponse(val legality: MoveLegality, val sideEffects: List<BoardModification>) {

    companion object {
        fun illegalBecause(reason: IllegalMoveReason) = MoveResponse(MoveLegality.illegalBecause(reason), emptyList())
        fun legalNoSideEffects() = MoveResponse(legal(), emptyList())
        fun legalWithSideEffects(sideEffects: List<BoardModification>) = MoveResponse(legal(), sideEffects)
    }
}


internal class MoveLegality private constructor(val illegalReason: IllegalMoveReason?) {

    val legal = (illegalReason == null)

    fun assertLegal() {
        illegalReason?.apply { throw IllegalMoveException(this) }
    }

    companion object {
        fun legal() = MoveLegality(null)
        fun illegalBecause(reason: IllegalMoveReason) = MoveLegality(reason)
    }
}

enum class IllegalMoveReason {
    NO_PIECE_FOUND, INVALID_PIECE_SIDE, ILLEGAL_MOVE_SHAPE, OPPONENT_PIECE_IN_THE_WAY, FRIEND_PIECE_IN_THE_WAY
}

internal fun IllegalMoveReason?.asMoveLegality() = if (this != null) MoveLegality.illegalBecause(this) else legal()