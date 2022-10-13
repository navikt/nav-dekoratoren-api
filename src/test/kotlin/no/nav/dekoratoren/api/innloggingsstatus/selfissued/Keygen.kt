package no.nav.dekoratoren.api.innloggingsstatus.selfissued

import java.security.SecureRandom

private val allowed = ('A'..'Z') + ('a'..'z') + ('0'..'9')

internal fun generateRandomKey(byteSize: Int = 64): String {
    val random = SecureRandom()
    return (1..byteSize).map { allowed[random.nextInt(allowed.size)] }.joinToString("")
}
