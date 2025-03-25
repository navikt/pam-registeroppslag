package no.nav.arbeid.registeroppslag

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.logging.LogbackMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.binder.system.UptimeMetrics
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import no.nav.arbeid.registeroppslag.bemanningsforetak.BemanningsforetakController
import no.nav.arbeid.registeroppslag.bemanningsforetak.BemanningsforetakParser
import no.nav.arbeid.registeroppslag.bemanningsforetak.BemanningsforetakService
import no.nav.arbeid.registeroppslag.config.TokenConfig
import no.nav.arbeid.registeroppslag.metrikker.Metrikker
import no.nav.arbeid.registeroppslag.nais.HealthService
import no.nav.arbeid.registeroppslag.nais.NaisController
import no.nav.arbeid.registeroppslag.renholdsvirksomhet.RenholdController
import no.nav.arbeid.registeroppslag.renholdsvirksomhet.RenholdParser
import no.nav.arbeid.registeroppslag.renholdsvirksomhet.RenholdService
import no.nav.arbeid.registeroppslag.scheduler.Scheduler
import no.nav.arbeid.registeroppslag.valkey.ValkeyService
import no.nav.arbeid.registeroppslag.valkey.opprettPool
import java.net.http.HttpClient
import java.time.Duration
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
open class ApplicationContext(envInn: Map<String, String>) {

    val env: Map<String, String> = envInn

    val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .setTimeZone(TimeZone.getTimeZone("Europe/Oslo"))

    val xmlMapper: ObjectMapper = XmlMapper().registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .setTimeZone(TimeZone.getTimeZone("Europe/Oslo"))

    val prometheusRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT).also { registry ->
        ClassLoaderMetrics().bindTo(registry)
        JvmMemoryMetrics().bindTo(registry)
        JvmGcMetrics().bindTo(registry)
        JvmThreadMetrics().bindTo(registry)
        UptimeMetrics().bindTo(registry)
        ProcessorMetrics().bindTo(registry)
        LogbackMetrics().bindTo(registry)
    }

    val metrikker = Metrikker(prometheusRegistry)

    val httpClient: HttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .version(HttpClient.Version.HTTP_1_1)
        .build()

    val tokenConfig = TokenConfig(env)

    val healthService = HealthService()

    val leaderElector = LeaderElector(
        env.getValue("ELECTOR_GET_URL"),
        env["NO_LEADER_ELECTION"]?.toBoolean() ?: false,
        objectMapper,
    )
    val naisController = NaisController(healthService, prometheusRegistry, leaderElector)

    open val scheduler = Scheduler("0 0 6 * * ?") { // Kjører kl 06:00 UTC
        repeat(3) {
            try {
                require(leaderElector.erLeader()) { "Er ikke leader" }
                bemanningsforetakService.lastNedOgLagreRegister()
                renholdService.lastNedOgLagreRegister()
                return@Scheduler
            } catch (e: Exception) {
                Scheduler.log.warn("Feil i scheduler: ${e.message}, forsøker igjen for ${it.inc()}. gang om 2 minutter", e)
                Thread.sleep(Duration.ofMinutes(2))
            }
        }
    }

    val valkey = ValkeyService(opprettPool(env))

    val bemanningsforetakParser = BemanningsforetakParser(objectMapper)
    open val bemanningsforetakService by lazy {
        BemanningsforetakService(
            parser = bemanningsforetakParser,
            httpClient = httpClient,
            valkey = valkey,
            objectMapper = objectMapper,
            metrikker = metrikker,
            bemanningsforetakRegisterUrl = env.getValue("BEMANNINGSFORETAKSREGISTER_URL")
        )
    }
    open val bemanningsforetakController by lazy { BemanningsforetakController(bemanningsforetakService) }

    val renholdParser = RenholdParser(xmlMapper)
    open val renholdService by lazy {
        RenholdService(
            parser = renholdParser,
            httpClient = httpClient,
            valkey = valkey,
            objectMapper = objectMapper,
            metrikker = metrikker,
            renholdsregisterURL = env.getValue("RENHOLDSREGISTER_URL")
        )
    }
    open val renholdController by lazy { RenholdController(renholdService) }
}
