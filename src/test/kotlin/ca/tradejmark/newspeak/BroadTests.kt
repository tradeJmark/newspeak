package ca.tradejmark.newspeak

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.PrintStream

class BroadTests {
    private fun stringWrapIO(s: String): ByteArrayOutputStream = wrapIO(ByteArrayInputStream(s.toByteArray()))

    private fun resWrapIO(r: String): ByteArrayOutputStream = wrapIO(javaClass.classLoader.getResourceAsStream(r))

    private fun wrapIO(i: InputStream?): ByteArrayOutputStream {
        val outStream = ByteArrayOutputStream()
        System.setIn(i)
        System.setOut(PrintStream(outStream))
        return outStream
    }

    @Test
    fun basicWhileTest() {
        val outStream = resWrapIO("basic_while.nsp")
        main(arrayOf())
        val lines = outStream.toString().lines()
        for (i in 0 until 3) {
            assertEquals(i.toString(), lines[i].trim())
        }
    }

    @Test
    fun nestedWhileTest() {
        val outStream = resWrapIO("nested_while.nsp")
        main(arrayOf())
        val lines = outStream.toString().lines()
        var i = -1
        for (x in 0 until 5) {
            i += 1
            assertEquals(x.toString(), lines[i])
            for (y in 0 until x) {
                i += 1
                assertEquals(y.toString(), lines[i])
            }
        }
    }

    @Test
    fun helloWorldTest() {
        val outStream = stringWrapIO("Print \"Hello World!\".")
        main(arrayOf())
        val out = outStream.toString().trim()
        assertEquals("Hello World!", out)
    }

    @Test
    fun basicIfTest() {
        val outStream = resWrapIO("basic_if.nsp")
        main(arrayOf())
        val out = outStream.toString().trim()
        assertEquals("2", out)
    }

    @Test
    fun basicForEachTest() {
        val outStream = resWrapIO("basic_foreach.nsp")
        main(arrayOf())
        val lines = outStream.toString().lines().map(String::trim)
        val l = listOf(1, 2, 5)
        for (i in 0..2) {
            assertEquals(l[i].toString(), lines[i])
        }
    }

    @Test
    fun basicFunctionRunTest() {
        val outputStream = resWrapIO("basic_func_run.nsp")
        main(arrayOf())
        val out = outputStream.toString().trim()
        assertEquals((7/3.14).toString(), out)
    }

    @Test
    fun basicResultFunTest() {
        val outputStream = resWrapIO("basic_result_fun.nsp")
        main(arrayOf())
        val out = outputStream.toString().trim()
        assertEquals(21.toString(), out)
    }
}
