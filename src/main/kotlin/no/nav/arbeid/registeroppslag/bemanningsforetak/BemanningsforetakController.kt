package no.nav.arbeid.registeroppslag.bemanningsforetak

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import no.nav.arbeid.registeroppslag.Organisasjonsnummer
import no.nav.arbeid.registeroppslag.sikkerhet.Rolle

class BemanningsforetakController(
    private val bemanningsforetakService: BemanningsforetakService,
) {
    fun setupRoutes(javalin: Javalin) {
        javalin.get("/api/bemanningsforetak/lastned", { lastNedOgLagreRegister(it) }, Rolle.PÅLOGGET)
        javalin.get("/api/bemanningsforetak/{orgnr}", { hentBemanningsforetak(it) }, Rolle.PÅLOGGET)
        javalin.get("/api/bemanningsforetak/{orgnr}/status", { hentBemanningsforetakStatus(it) }, Rolle.PÅLOGGET)
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
        ctx.status(HttpStatus.OK)
    }
}