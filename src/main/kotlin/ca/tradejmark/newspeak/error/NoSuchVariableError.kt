package ca.tradejmark.newspeak.error

class NoSuchVariableError(varName: String): Error("Variable '$varName' does not exist.")