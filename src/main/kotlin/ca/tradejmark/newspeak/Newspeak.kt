package ca.tradejmark.newspeak

import ca.tradejmark.newspeak.error.IndentationError
import ca.tradejmark.newspeak.util.prefixOf
import org.antlr.v4.runtime.*
import java.io.File
import java.lang.StringBuilder
import java.util.*

const val INDENT = '`'
const val DEDENT = '~'
const val BLOPEN = ':'

fun main(args: Array<String>) {
    val stdIn = Scanner(System.`in`)
    val sc = when (args.size) {
        0 -> stdIn
        else -> Scanner(File(args[0]))
    }
    val int = NSInterpreter(stdIn)
    var bufferedLine = ""
    var lineFetched = false;
    while (lineFetched || sc.hasNextLine()) {
        var line = if (!lineFetched) {
            sc.nextLine().trim()
        }
        else {
            lineFetched = false
            bufferedLine
        }
        if (line.isEmpty()) continue
        if (line.endsWith(BLOPEN)) {
            line = StringBuilder().apply {
                appendLine(line)
                bufferedLine = getBlock(sc)
                lineFetched = true
            }.toString()
        }

        val chars = CharStreams.fromString(line)
        val lexer = NewspeakLexer(chars);
        val tokens = CommonTokenStream(lexer)
        val parser = NewspeakParser(tokens)
        int.visitLine(parser.line())
    }
}

fun StringBuilder.getBlock(sc: Scanner): String {
    var line = sc.nextLine()
    val indent = when (line.first()) {
        ' ' -> line.prefixOf(' ')
        '\t' -> line.prefixOf('\t')
        else -> throw IndentationError("Missing indentation at $line.")
    }
    append(INDENT)
    while (line.startsWith(indent)) {
        line = line.trim()
        appendLine(line)
        line = if (line.endsWith(BLOPEN)) {
            getBlock(sc)
        }
        else try {
            sc.nextLine()
        }
        catch (e: NoSuchElementException) {
            ""
        }
    }
    append(DEDENT)
    if (line.toLowerCase().startsWith("otherwise")) {
        appendLine(line)
        if (line.endsWith(":")) line = getBlock(sc)
        else line = sc.nextLine()
    }
    return line
}