package net.binarysailor.chesslounge.client

import java.util.UUID.randomUUID

abstract class Command(messageId: MessageID)
class SeekPlayCommand(messageId: MessageID) : Command(messageId) {
    val message: String = "seekplay"
    constructor() : this(MessageID(randomUUID()))
}

class MoveCommand(messageId: MessageID, private val gameId: GameID, private val moveSymbol: String) : Command(messageId) {
    constructor(gameId: GameID, moveSymbol: String) : this(MessageID(randomUUID()), gameId, moveSymbol)
    val message: String
        get() = "game ${this.gameId.id} move $moveSymbol"
}