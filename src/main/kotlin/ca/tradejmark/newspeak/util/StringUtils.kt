package ca.tradejmark.newspeak.util

fun String.prefixOf(char: Char): String = takeWhile { it == char }