package no.nav.arbeid.registeroppslag.renholdsvirksomhet

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeid.registeroppslag.Organisasjonsnummer
import no.nav.arbeid.registeroppslag.RegisterstatusDTO
import no.nav.arbeid.registeroppslag.metrikker.Metrikker
import no.nav.arbeid.registeroppslag.valkey.ValkeyService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient

class RenholdService(
    private val parser: RenholdParser,
    private val httpClient: HttpClient,
    private val valkey: ValkeyService,
    private val objectMapper: ObjectMapper,
    private val metrikker: Metrikker,
    private val renholdsregisterURL: String,
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(RenholdService::class.java)
        val registernavn = RenholdsvirksomhetDTO.registernavn
    }

    fun hentRenholdsvirksomhet(organisasjonsnummer: Organisasjonsnummer): RenholdsvirksomhetDTO {
        val respons = valkey.get("${organisasjonsnummer}:$registernavn")
        val renholdsvirksomhet = respons?.let { objectMapper.readValue(it, RenholdsvirksomhetDTO::class.java) }
            ?: RenholdsvirksomhetDTO.ikkeRegistrert().copy(organisasjonsnummer = organisasjonsnummer)
        log.info("Henter renholdsvirksomhet $renholdsvirksomhet")
        return renholdsvirksomhet
    }

    fun hentRenholdsvirksomhetStatus(organisasjonsnummer: Organisasjonsnummer): RegisterstatusDTO {
        return hentRenholdsvirksomhet(organisasjonsnummer).tilRegisterstatus()
    }

    fun lastNedOgLagreRegister() {
        val renholdsregisteret = lastNedRegister()
        lagreRegister(renholdsregisteret)
        metrikker.lastetNedOgLagretRenholdsregister()
    }

    fun lagreRegister(register: List<RenholdsvirksomhetDTO>) {
        register.map { valkey.set("${it.organisasjonsnummer}:$registernavn", objectMapper.writeValueAsString(it)) }
        log.info("Lagret ${register.size} resultater fra renholdsregisteret")
    }

    fun lastNedRegister(): List<RenholdsvirksomhetDTO> {
        val url = URI(renholdsregisterURL)
        val registerData = parser.lastNedRegisterData(registernavn, url, httpClient)
        val renholdsregisteret = parser.parseRegister(registerData)
        return renholdsregisteret
    }
}