package ca.tradejmark.newspeak

import ca.tradejmark.newspeak.NSValue.*
import ca.tradejmark.newspeak.NSValue.NSNum.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.io.*
import java.util.*

class StmtTests {
    lateinit var int: NSInterpreter

    @BeforeEach
    fun before() {
        val s = ByteArrayInputStream.nullInputStream()
        int = NSInterpreter(Scanner(s))
    }

    private fun useStdIn(text: String) {
        int = NSInterpreter(Scanner(text))
    }

    @Test
    fun setIntLiteralTest() {
        val parser = obtainParser("Set x to 7.")
        int.visitLine(parser.line())
        assertEquals(NSInt(7), int.eval.scope["x"])
    }

    @Test
    fun setListPosTest() {
        val l = NSList()
        l.add(NSInt(5))
        l.add(NSInt(7))
        int.eval.scope["list"] = l
        val parser = obtainParser("Set list at position 1 to \"text\"")
        int.visitLine(parser.line())
        assertEquals(NSString("text"), l[1])
    }

    @Test
    fun setRealLiteralTest() {
        val parser = obtainParser("Set x to 7.3.")
        int.visitLine(parser.line())
        assertEquals(NSReal(7.3), int.eval.scope["x"])
    }

    @Test
    fun incTest() {
        int.eval.scope["x"] = NSInt(7)
        val parser = obtainParser("Increment x.")
        int.visitLine(parser.line())
        assertEquals(NSInt(8), int.eval.scope["x"])
    }

    @Test
    fun printIntLiteralTest() {
        val parser = obtainParser("Print 7.")
        val out = ByteArrayOutputStream()
        System.setOut(PrintStream(out))
        int.visitLine(parser.line())
        assertEquals("7", out.toString().trim())
    }

    @Test
    fun printIntVarTest() {
        int.eval.scope["x"] = NSInt(7)
        val parser = obtainParser("Print x.")
        val out = ByteArrayOutputStream()
        System.setOut(PrintStream(out))
        int.visitLine(parser.line())
        assertEquals("7", out.toString().trim())
    }

    @Test
    fun readStringTest() {
        val parser = obtainParser("Read into x.")
        useStdIn("some text\n")
        int.visitLine(parser.line())
        assertEquals(NSString("some text"), int.eval.scope["x"])
    }

    @Test
    fun readIntTest() {
        val parser = obtainParser("Read an integer into x.")
        useStdIn("12\n")
        int.visitLine(parser.line())
        assertEquals(NSInt(12), int.eval.scope["x"])
    }

    @Test
    fun whatStringTest() {
        val parser = obtainParser("what is \"test\"?")
        val out = ByteArrayOutputStream()
        System.setOut(PrintStream(out))
        int.visitLine(parser.line())
        assertEquals("\"test\"", out.toString().trim())
    }

    @Test
    fun whatRealTest() {
        val parser = obtainParser("What is 7?")
        val out = ByteArrayOutputStream()
        System.setOut(PrintStream(out))
        int.visitLine(parser.line())
        assertEquals("7", out.toString().trim())
    }

    @Test
    fun ifElseTest() {
        int.eval.scope["x"] = NSInt(7)
        int.eval.scope["y"] = NSInt(0)
        val parser = obtainParser("if x is equal to 6:\n`y is 7~\notherwise:\n`y is 8.~")
        int.visitLine(parser.line())
        assertEquals(NSInt(8), int.eval.scope["y"])
    }

    @Test
    fun addTest() {
        val l = NSList()
        int.eval.scope["list"] = l
        l.add(NSInt(7))
        val parser = obtainParser("Add 9 to list.")
        int.visitLine(parser.line())
        assertEquals(NSInt(9), l[1])
    }
}