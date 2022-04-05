package no.nav.personbruker.innloggingsstatus.sts

class STSException : Exception {
    constructor() : super()
    constructor(message: String, cause: Throwable) : super(message, cause)
}
