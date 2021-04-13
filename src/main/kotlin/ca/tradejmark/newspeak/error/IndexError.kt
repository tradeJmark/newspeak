package ca.tradejmark.newspeak.error

class IndexError(i: Int, op: String): Error("Cannot $op value at index $i.")