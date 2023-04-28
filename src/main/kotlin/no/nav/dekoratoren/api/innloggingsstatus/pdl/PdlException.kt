package no.nav.dekoratoren.api.innloggingsstatus.pdl

open class PdlException : Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, e: Throwable) : super(message, e)
}