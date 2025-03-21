package no.nav.arbeid.registeroppslag.nais

import io.javalin.Javalin
import io.javalin.http.HttpStatus
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.prometheus.client.exporter.common.TextFormat
import no.nav.arbeid.registeroppslag.LeaderElector
import no.nav.arbeid.registeroppslag.sikkerhet.Rolle

class NaisController(
    private val healthService: HealthService,
    private val prometheusMeterRegistry: PrometheusMeterRegistry,
    private val leaderElector: LeaderElector,
) {
    fun setupRoutes(javalin: Javalin) {
        javalin.get("/internal/isReady", { it.status(200) }, Rolle.UNPROTECTED)
        javalin.get(
            "/internal/isAlive",
            { if (healthService.isHealthy()) it.status(HttpStatus.OK) else it.status(HttpStatus.SERVICE_UNAVAILABLE) },
            Rolle.UNPROTECTED
        )
        javalin.get(
            "/internal/prometheus",
            { it.contentType(TextFormat.CONTENT_TYPE_004).result(prometheusMeterRegistry.scrape()) },
            Rolle.UNPROTECTED
        )
        javalin.get("/internal/leader", { it.json(leaderElector.erLeader()) }, Rolle.UNPROTECTED)
    }
}

