package net.binarysailor.chessengine

import net.binarysailor.chesslounge.engine.Square
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.text.ParseException
import kotlin.test.assertEquals

class SquareTest {
    @ParameterizedTest
    @ValueSource(strings = ["e3,5,3", "a7,1,7", "g4,7,4", "h8,8,8"])
    fun `should parse correct square names`(params: String) {
        val split = params.split(",")
        val squareName = split[0]
        val expectedFile = split[1].toInt()
        val expectedRank = split[2].toInt()

        val square = Square.of(squareName)
        assertEquals(expectedRank, square.rank)
        assertEquals(expectedFile, square.file)
    }

    @ParameterizedTest
    @ValueSource(strings = ["e9", "a0", "b", "ck"])
    fun `should reject incorrect square names`(squareName: String) {
        assertThrows<ParseException> { Square.of(squareName)
        }.also { print(it.message) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["a3", "f8", "h4", "b1", "C3", "D7"])
    fun `should print square names`(squareName: String) {
        assertEquals(squareName.lowercase(), Square.of(squareName).name())
    }
}