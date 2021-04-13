package ca.tradejmark.newspeak

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

fun obtainParser(from: String): NewspeakParser {
    val chars = CharStreams.fromString(from)
    val lex = NewspeakLexer(chars)
    val tokens = CommonTokenStream(lex)
    return NewspeakParser(tokens)
}