package ca.tradejmark.newspeak.error

class UnsupportedOperatorError(type: String, operator: String): Error("$type does not support operator '$operator'")