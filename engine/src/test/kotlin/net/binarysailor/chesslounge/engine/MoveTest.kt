package net.binarysailor.chesslounge.engine

import net.binarysailor.chesslounge.engine.Move
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class MoveTest {

    @Test
    fun `long move along a rank to the right should build a correct path`() {
        val c2h2Path = Move("c2", "h2").path().map { it.name() }
        assertEquals(6, c2h2Path.size)
        assertContains(c2h2Path, "c2")
        assertContains(c2h2Path, "d2")
        assertContains(c2h2Path, "e2")
        assertContains(c2h2Path, "f2")
        assertContains(c2h2Path, "g2")
        assertContains(c2h2Path, "h2")
    }

    @Test
    fun `short move along a rank to the left should build a correct path`() {
        val b1a1Path = Move("b1", "a1").path().map { it.name() }
        assertEquals(2, b1a1Path.size)
        assertContains(b1a1Path, "b1")
        assertContains(b1a1Path, "a1")
    }

    @Test
    fun `move along a diagonal should build a correct path`() {
        val a1d4Path = Move("a1", "d4").path().map { it.name() }
        assertEquals(4, a1d4Path.size)
        assertContains(a1d4Path, "a1")
        assertContains(a1d4Path, "b2")
        assertContains(a1d4Path, "c3")
        assertContains(a1d4Path, "d4")
    }

    @Test
    fun `knight move should build one of acceptable paths`() {
        val a1b3Path = Move("a1", "b3").path().map { it.name() }
        assertEquals(3, a1b3Path.size)
        assertContains(a1b3Path, "a1")
        assert(a1b3Path.contains("a2") || a1b3Path.contains("b2"))
        assertContains(a1b3Path, "b3")
    }
}