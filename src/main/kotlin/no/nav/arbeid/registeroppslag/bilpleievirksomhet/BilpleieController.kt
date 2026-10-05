package no.nav.arbeid.registeroppslag.bilpleievirksomhet

import io.javalin.config.JavalinConfig
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import no.nav.arbeid.registeroppslag.Organisasjonsnummer
import no.nav.arbeid.registeroppslag.sikkerhet.Rolle

class BilpleieController(
    private val bilpleieService: BilpleieService,
) {
    fun setupRoutes(config: JavalinConfig) {
        config.routes.get("/api/bilpleieregister/lastned", { lastNedOgLagreRegister(it) }, Rolle.PÅLOGGET)
        config.routes.get("/api/bilpleievirksomhet/{orgnr}", { hentBilpleievirksomhet(it) }, Rolle.PÅLOGGET)
        config.routes.get("/api/bilpleievirksomhet/{orgnr}/status", { hentBilpleievirksomhetStatus(it) }, Rolle.PÅLOGGET)
    }

    fun hentBilpleievirksomhet(ctx: Context) {
        val orgnr = Organisasjonsnummer(ctx.pathParam("orgnr"))
        val bilpleievirksomhet = bilpleieService.hentBilpleievirksomhet(orgnr)
        ctx.json(bilpleievirksomhet)
    }

    fun hentBilpleievirksomhetStatus(ctx: Context) {
        val orgnr = Organisasjonsnummer(ctx.pathParam("orgnr"))
        val status = bilpleieService.hentBilpleievirksomhetStatus(orgnr)
        ctx.json(status)
    }

    fun lastNedOgLagreRegister(ctx: Context) {
        bilpleieService.lastNedOgLagreRegister()
        ctx.apply {
            status(HttpStatus.OK)
            result("Lastet ned og lagret ${BilpleievirksomhetDTO.registernavn}")
            contentType("text/plain")
        }
    }
}
