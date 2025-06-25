package no.nav.arbeid.registeroppslag.bilpleievirksomhet

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeid.registeroppslag.Organisasjonsnummer
import no.nav.arbeid.registeroppslag.RegisterstatusDTO
import no.nav.arbeid.registeroppslag.metrikker.Metrikker
import no.nav.arbeid.registeroppslag.valkey.ValkeyService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient

class BilpleieService(
    private val parser: BilpleieParser,
    private val httpClient: HttpClient,
    private val valkey: ValkeyService,
    private val objectMapper: ObjectMapper,
    private val metrikker: Metrikker,
    private val bilpleieregisterURL: String,
    private val bilverkstedURL: String,
    ) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(BilpleieService::class.java)
        val registernavn = BilpleievirksomhetDTO.registernavn
    }

    fun hentBilpleievirksomhet(organisasjonsnummer: Organisasjonsnummer): BilpleievirksomhetDTO {
        val respons = valkey.get("${organisasjonsnummer}:$registernavn")
        val bilpleievirksomhet = respons?.let { objectMapper.readValue(it, BilpleievirksomhetDTO::class.java) }
            ?: BilpleievirksomhetDTO.ikkeRegistrert().copy(organisasjonsnummer = organisasjonsnummer)
        log.info("Henter bilpleievirksomhet $bilpleievirksomhet")
        return bilpleievirksomhet
    }

    fun hentBilpleievirksomhetStatus(organisasjonsnummer: Organisasjonsnummer): RegisterstatusDTO {
        return hentBilpleievirksomhet(organisasjonsnummer).tilRegisterstatus()
    }

    fun lastNedOgLagreRegister() {
        val bilpleieregisteret = lastNedRegister()
        lagreRegister(bilpleieregisteret)
        metrikker.lastetNedOgLagretBilpleieregister()
    }

    fun lagreRegister(register: List<BilpleievirksomhetDTO>) {
        register.map { valkey.set("${it.organisasjonsnummer}:$registernavn", objectMapper.writeValueAsString(it)) }
        log.info("Lagret ${register.size} resultater fra bilpleieregisteret")
    }

    fun lastNedRegister(): List<BilpleievirksomhetDTO> {
        val url = URI(bilpleieregisterURL)
        val headers = mapOf("Content-Version" to "1.2")
        val registerData = parser.lastNedRegisterData(registernavn, url, httpClient, headers)
        val bilpleieregisteret = parser.parseRegister(registerData)

        val bilverkstedURL = URI(bilverkstedURL)
        val bilverkstedData = parser.lastNedRegisterData("bilverksted", bilverkstedURL, httpClient)
        val bilverksted = parser.parseBilverksted(bilverkstedData)

        return bilpleieregisteret + bilverksted
    }
}
