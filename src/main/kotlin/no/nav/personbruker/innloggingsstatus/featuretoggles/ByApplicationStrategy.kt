package no.nav.personbruker.innloggingsstatus.featuretoggles

import no.finn.unleash.strategy.Strategy
import no.nav.common.utils.EnvironmentUtils

class ByApplicationStrategy : Strategy {

    override fun getName(): String = "byApplication"

    override fun isEnabled(parameters: Map<String, String>): Boolean {
        return getThisAppName().let { appName ->
            parameters.getActiveAppNames().any { activeAppName ->
                activeAppName == appName
            }
        }
    }

    private fun getThisAppName(): String? {
        return EnvironmentUtils.getApplicationName().orElse(null)
    }

    private fun Map<String, String>.getActiveAppNames(): List<String> {
        return get("app")
            ?.split(",")
            ?: emptyList()
    }
}
