package no.nav.personbruker.innloggingsstatus.varsel

import no.nav.tms.token.support.azure.exchange.AzureService

class VarselbjelleTokenFetcher(
    private val azureService: AzureService,
    private val varselbjelleClientId: String
) {
    suspend fun fetchToken(): String {
        return azureService.getAccessToken(varselbjelleClientId)
    }
}
