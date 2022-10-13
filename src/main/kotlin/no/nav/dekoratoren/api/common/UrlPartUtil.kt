package no.nav.dekoratoren.api.common

object UrlPartUtil {

    fun parseDomain(origin: String): String {
        val originRegex = "^(.+)://([^/\\?]+)(.+)?$".toRegex()

        return originRegex.find(origin)?.destructured?.let { (_, domain) ->
            domain
        }?: ""
    }
}