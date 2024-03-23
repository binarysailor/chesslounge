package net.binarysailor.chesslounge.engine.exception

class MoveParseException(moveSymbol: String) : IllegalArgumentException("Can't parse '${moveSymbol}' as a move")