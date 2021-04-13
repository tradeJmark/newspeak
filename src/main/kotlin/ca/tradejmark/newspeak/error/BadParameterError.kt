package ca.tradejmark.newspeak.error

class BadParameterError: Error {
    constructor(pName: String): super("Parameter $pName is not valid.")
    constructor(exp: Int, given: Int): super("Expected $exp arguments, was given $given.")
}