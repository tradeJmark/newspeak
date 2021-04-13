package ca.tradejmark.newspeak.error

class WrongTypeError(expr: String, type: String): Error("'$expr' must result in a $type value")