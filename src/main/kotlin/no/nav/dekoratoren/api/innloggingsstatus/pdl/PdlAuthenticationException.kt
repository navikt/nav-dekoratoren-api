package no.nav.dekoratoren.api.innloggingsstatus.pdl

class PdlAuthenticationException: PdlException {
    constructor() : super()
    constructor(message: String) : super(message)
}