package no.nav.personbruker.innloggingsstatus.config

import io.ktor.application.*
import io.ktor.http.HttpHeaders.Origin
import io.ktor.request.*
import io.ktor.server.netty.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import io.netty.handler.codec.http.DefaultHttpHeaders
import io.netty.handler.codec.http.HttpHeaders

class MaskOriginSubDomain(config: Config) {

    private val originPatterns = config.originPatterns

    @OptIn(InternalAPI::class)
    fun intercept(context: PipelineContext<Unit, ApplicationCall>) {

        val request = context.call.request

        if (request.headers !is NettyApplicationRequestHeaders) {
            return
        }

        val origin = request.headers.getAll(Origin)?.singleOrNull()

        if (origin != null) {
            removeOriginSubDomain(request, origin)
        }

    }

    @OptIn(InternalAPI::class)
    private val ApplicationRequest.mutableHeaders: HttpHeaders get() {
        // Use reflection to access private field 'headers' contained in instance of NettyApplicationRequestHeaders
        return NettyApplicationRequestHeaders::class.java.getDeclaredField("headers").let {
            it.isAccessible = true
            val backingField = it.get((headers)) as DefaultHttpHeaders
            it.isAccessible = false
            backingField
        }
    }

    private fun removeOriginSubDomain(request: ApplicationRequest, origin: String) {
        val headers = request.mutableHeaders

        for (pattern in originPatterns) {
            pattern.find(origin)?.destructured?.let { (schema, host) ->
                headers[Origin] = "$schema://$host"
            }
        }
    }

    class Config {
        val originPatterns = mutableSetOf<Regex>()

        fun host(host: String, schemes: List<String> = listOf("http")) {
            if (host == "*") {
                return
            }

            require("://" !in host) { "Schema skal ikke inkluderes i host-strengen direkte." }

            for (schema in schemes) {
                val hostPattern = host.replace(".", "\\.")

                originPatterns.add("^($schema)://.*\\.($hostPattern.*)\$".toRegex())
            }
        }
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Config, MaskOriginSubDomain> {

        override val key: AttributeKey<MaskOriginSubDomain> = AttributeKey("MaskOriginSubDomain")

        override fun install(pipeline: ApplicationCallPipeline, configure: Config.() -> Unit): MaskOriginSubDomain {
            val maskOriginSubDomain = MaskOriginSubDomain(Config().apply(configure))
            pipeline.intercept(ApplicationCallPipeline.Features) { maskOriginSubDomain.intercept(this) }
            return maskOriginSubDomain
        }
    }
}