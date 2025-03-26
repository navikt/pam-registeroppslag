package no.nav.arbeid.registeroppslag.renholdsvirksomhet

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import no.nav.arbeid.registeroppslag.Organisasjonsnummer
import no.nav.arbeid.registeroppslag.sikkerhet.Rolle

class RenholdController(
    private val renholdService: RenholdService,
) {
    fun setupRoutes(javalin: Javalin) {
        javalin.get("/api/renholdsvirksomhet/lastned", { lastNedOgLagreRegister(it) }, Rolle.PÅLOGGET)
        javalin.get("/api/renholdsvirksomhet/{orgnr}", { hentRenholdsvirksomhet(it) }, Rolle.PÅLOGGET)
        javalin.get("/api/renholdsvirksomhet/{orgnr}/status", { hentRenholdsvirksomhetStatus(it) }, Rolle.PÅLOGGET)
    }

    fun hentRenholdsvirksomhet(ctx: Context) {
        val orgnr = Organisasjonsnummer(ctx.pathParam("orgnr"))
        ctx.json(renholdService.hentRenholdsvirksomhet(orgnr))
    }

    fun hentRenholdsvirksomhetStatus(ctx: Context) {
        val orgnr = Organisasjonsnummer(ctx.pathParam("orgnr"))
        ctx.json(renholdService.hentRenholdsvirksomhetStatus(orgnr))
    }

    fun lastNedOgLagreRegister(ctx: Context) {
        renholdService.lastNedOgLagreRegister()
        ctx.apply {
            status(HttpStatus.OK)
            result("Lastet ned og lagret ${RenholdsvirksomhetDTO.registernavn}")
            contentType("text/plain")
        }
    }
}