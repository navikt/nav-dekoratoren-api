package no.nav.personbruker.innloggingsstatus.health

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.html.respondHtml
import kotlinx.coroutines.coroutineScope
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.title
import kotlinx.html.tr

suspend fun ApplicationCall.buildSelftestPage(selfTests: List<SelfTest>) = coroutineScope {

    val externalServiceStatuses = selfTests.map {
        StatusSnapshot(it.externalServiceStatus(), it.externalServiceName)
    }
    val hasFailedChecks = externalServiceStatuses.any { healthStatus -> ServiceStatus.ERROR == healthStatus.serviceStatus }

    respondHtml(status =
    if (hasFailedChecks) {
        HttpStatusCode.ServiceUnavailable
    } else {
        HttpStatusCode.OK
    })
    {
        head {
            title { +"Selftest innloggingsstatus" }
        }
        body {
            val text = if (hasFailedChecks) {
                "FEIL"
            } else {
                "Service-status: OK"
            }
            h1 {
                style = if (hasFailedChecks) {
                    "background: red;font-weight:bold"
                } else {
                    "background: green"
                }
                +text
            }
            table {
                thead {
                    tr { th { +"SELFTEST INNLOGGINGSSTATUS" } }
                }
                tbody {
                    externalServiceStatuses.map {
                        tr {
                            td { +it.serviceName }
                            td {
                                val status = it.serviceStatus
                                style = if (status == ServiceStatus.OK) {
                                    "background: green"
                                } else {
                                    "background: red;font-weight:bold"
                                }
                                +it.serviceStatus.name
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class StatusSnapshot(
    val serviceStatus: ServiceStatus,
    val serviceName: String
)
