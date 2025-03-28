package no.nav.arbeid.registeroppslag.bemanningsforetak

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeid.registeroppslag.Organisasjonsnummer
import no.nav.arbeid.registeroppslag.RegisterstatusDTO
import no.nav.arbeid.registeroppslag.metrikker.Metrikker
import no.nav.arbeid.registeroppslag.valkey.ValkeyService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient

class BemanningsforetakService(
    private val parser: BemanningsforetakParser,
    private val httpClient: HttpClient,
    private val valkey: ValkeyService,
    private val objectMapper: ObjectMapper,
    private val metrikker: Metrikker,
    private val bemanningsforetakRegisterUrl: String,
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(BemanningsforetakService::class.java)
        val registernavn = BemanningsforetakDTO.registernavn
    }

    fun hentBemanningsforetak(organisasjonsnummer: Organisasjonsnummer): BemanningsforetakDTO {
        val resonseValue = valkey.get("${organisasjonsnummer}:$registernavn")
        val bemanningsforetak = resonseValue?.let { objectMapper.readValue(it, BemanningsforetakDTO::class.java) }
            ?: BemanningsforetakDTO.ikkeRegistrert()
        log.info("Henter bemanningsforetak $bemanningsforetak")
        return bemanningsforetak
    }

    fun hentBemanningsforetakStatus(organisasjonsnummer: Organisasjonsnummer): RegisterstatusDTO {
        val bemanningsforetak = hentBemanningsforetak(organisasjonsnummer)
        return bemanningsforetak.tilRegisterstatus()
    }

    fun lastNedOgLagreRegister() {
        val bemanningsforetaksregisteret = lastNedRegister()
        lagreRegister(bemanningsforetaksregisteret)
        metrikker.lastetNedOgLagretBemmaningsforetak()
    }

    fun lagreRegister(register: List<BemanningsforetakDTO>) {
        register.forEach {
            valkey.set("${it.organisasjonsnummer}:$registernavn", objectMapper.writeValueAsString(it))
        }
        log.info("Lagret ${register.size} resultater fra bemanningsforetaksregisteret")
    }

    fun lastNedRegister(): List<BemanningsforetakDTO> {
        val url = URI(bemanningsforetakRegisterUrl)
        val registerData = parser.lastNedRegisterData(registernavn, url, httpClient)
        val bemanningsforetaksregisteret = parser.parseRegister(registerData)
        return bemanningsforetaksregisteret
    }
}