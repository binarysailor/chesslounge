package net.binarysailor.chessengine

import kotlin.math.abs
import kotlin.math.max

data class Move(val from: Square, val to: Square) {

    constructor(fromName: String, toName: String): this(Square.of(fromName), Square.of(toName))

    fun rankDistance() = abs(from.rank - to.rank)
    fun fileDistance() = abs(from.file - to.file)

    fun path(): List<Square> {
        val fileDelta = to.file - from.file
        val rankDelta = to.rank - from.rank
        val stepCount = max(abs(fileDelta), abs(rankDelta))

        return 0.rangeTo(stepCount).map {
            val rank = from.rank + it * rankDelta / stepCount
            val file = from.file + it * fileDelta / stepCount
            Square(file, rank)
        }.toList()
    }

    override fun toString() = "$from-$to"

}