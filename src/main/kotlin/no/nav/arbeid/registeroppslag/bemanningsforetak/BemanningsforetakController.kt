package no.nav.arbeid.registeroppslag.bemanningsforetak

import io.javalin.config.JavalinConfig
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import no.nav.arbeid.registeroppslag.Organisasjonsnummer
import no.nav.arbeid.registeroppslag.sikkerhet.Rolle

class BemanningsforetakController(
    private val bemanningsforetakService: BemanningsforetakService,
) {
    fun setupRoutes(config: JavalinConfig) {
        config.routes.get("/api/bemanningsforetak/lastned", { lastNedOgLagreRegister(it) }, Rolle.PÅLOGGET)
        config.routes.get("/api/bemanningsforetak/{orgnr}", { hentBemanningsforetak(it) }, Rolle.PÅLOGGET)
        config.routes.get("/api/bemanningsforetak/{orgnr}/status", { hentBemanningsforetakStatus(it) }, Rolle.PÅLOGGET)
    }

    fun hentBemanningsforetak(ctx: Context) {
        val orgnr = Organisasjonsnummer(ctx.pathParam("orgnr"))
        val bemanningsforetak = bemanningsforetakService.hentBemanningsforetak(orgnr)
        ctx.json(bemanningsforetak)
    }

    fun hentBemanningsforetakStatus(ctx: Context) {
        val orgnr = Organisasjonsnummer(ctx.pathParam("orgnr"))
        val status = bemanningsforetakService.hentBemanningsforetakStatus(orgnr)
        ctx.json(status)
    }

    fun lastNedOgLagreRegister(ctx: Context) {
        bemanningsforetakService.lastNedOgLagreRegister()
        ctx.apply {
            status(HttpStatus.OK)
            result("Lastet ned og lagret bemanningsforetaksregisteret")
            contentType("text/plain")
        }
    }
}