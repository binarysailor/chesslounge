package net.binarysailor.chesslounge.engine.exception

import net.binarysailor.chesslounge.engine.IllegalMoveReason

data class IllegalMoveException(val reason: IllegalMoveReason) : IllegalArgumentException("Illegal move")