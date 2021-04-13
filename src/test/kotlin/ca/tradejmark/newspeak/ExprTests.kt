package ca.tradejmark.newspeak

import ca.tradejmark.newspeak.NSValue.NSBool.*
import ca.tradejmark.newspeak.NSValue.NSNum.*
import ca.tradejmark.newspeak.NSValue.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import kotlin.system.exitProcess

class ExprTests {
    lateinit var ev: NSEvaluator

    @BeforeEach
    fun before() {
        ev = NSEvaluator()
    }

    private fun evalExpr(str: String): NSValue {
        val parser = obtainParser(str)
        return ev.visitExpression(parser.expression())
    }

    @Test
    fun literalTest() {
        assertEquals(NSInt(7), evalExpr("7"))
        assertEquals(NSReal(7.9), evalExpr("7.9"))
        assertEquals(NSString("test"), evalExpr("\"test\""))
        assertEquals(NSTrue, evalExpr("true"))
        assertEquals(NSFalse, evalExpr("False"))
    }

    @Test
    fun gtTest() {
        var parser = obtainParser("4 > 3")
        assertSame(NSTrue, ev.visitExpression(parser.expression()))

        parser = obtainParser("4.3 > 4.2")
        assertSame(NSTrue, ev.visitExpression(parser.expression()))

        parser = obtainParser("4.01 > 4")
        assertSame(NSTrue, ev.visitExpression(parser.expression()))

        parser = obtainParser("3.99 > 4")
        assertSame(NSFalse, ev.visitExpression(parser.expression()))

        parser = obtainParser("4 > 4.01")
        assertSame(NSFalse, ev.visitExpression(parser.expression()))
    }
    @Test
    fun ltTest() {
        var parser = obtainParser("2 < 4")
        assertSame(NSTrue, ev.visitExpression(parser.expression()))

        parser = obtainParser("5.9 < 6")
        assertSame(NSTrue, ev.visitExpression(parser.expression()))

        parser = obtainParser("9 < 9.01")
        assertSame(NSTrue, ev.visitExpression(parser.expression()))

        parser = obtainParser("3.01 < 3")
        assertSame(NSFalse, ev.visitExpression(parser.expression()))

        parser = obtainParser("4 < 4.01")
        assertSame(NSTrue, ev.visitExpression(parser.expression()))
    }

    @Test
    fun eqTest() {
        var parser = obtainParser("4 = 4")
        assertSame(NSTrue, ev.visitExpression(parser.expression()))

        parser = obtainParser("4.0 = 4")
        assertSame(NSTrue, ev.visitExpression(parser.expression()))

        parser = obtainParser("4/3=(4*30)/(3*30)")
        assertSame(NSTrue, ev.visitExpression(parser.expression()))
    }

    @Test
    fun arithTest() {
        assertEquals(NSInt(7), evalExpr("3 + 4"))
        assertEquals(NSReal(3.3), evalExpr("5.3 - 2"))
        assertEquals(NSInt(35), evalExpr("5 * 7"))
        assertEquals(NSReal(2.0), evalExpr("4 divided by 2"))
        assertEquals(NSInt(8), evalExpr("2^3"))
    }

    @Test
    fun listTest() {
        val l = NSList()
        l.add(NSInt(7))
        l.add(NSString("text"))
        l.add(NSInt(12))
        assertEquals(l, evalExpr("[7, \"text\", 12]"))
    }

    @Test
    fun listAtPosTest() {
        val l = NSList()
        l.add(NSInt(7))
        l.add(NSString("text"))
        l.add(NSInt(12))
        ev.scope["list"] = l
        assertEquals(NSInt(12), evalExpr("list at position 2"))
    }

    @Test
    fun functionTest() {
        assert(evalExpr("a function given length, width:\n`Print length.~") is NSFunc)
    }
}