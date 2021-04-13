package ca.tradejmark.newspeak.error

class ParseError(problem: String): Error("Parsing failed at: $problem")