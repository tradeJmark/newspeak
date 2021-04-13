package ca.tradejmark.newspeak.error

class MismatchedOperandsError(ctx: String): Error("In $ctx: Mismatched operand types")