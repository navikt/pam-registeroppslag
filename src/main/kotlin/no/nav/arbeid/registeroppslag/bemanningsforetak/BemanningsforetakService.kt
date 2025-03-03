package no.nav.arbeid.registeroppslag.bemanningsforetak

import com.fasterxml.jackson.databind.ObjectMapper
import glide.api.GlideClient
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import no.nav.arbeid.registeroppslag.Organisasjonsnummer
import no.nav.arbeid.registeroppslag.RegisterstatusDTO
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient

class BemanningsforetakService(
    private val parser: BemanningsforetakParser,
    private val httpClient: HttpClient,
    private val valkey: GlideClient,
    private val objectMapper: ObjectMapper,
    private val bemanningsforetakRegisterUrl: String,
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(BemanningsforetakService::class.java)
    }

    fun hentBemanningsforetak(organisasjonsnummer: Organisasjonsnummer): BemanningsforetakDTO {
        val resonseValue = runBlocking { valkey.get("${organisasjonsnummer}:bemanningsforetaksregisteret").await() }
        val bemanningsforetak = resonseValue?.let { objectMapper.readValue(it, BemanningsforetakDTO::class.java) }
            ?: BemanningsforetakDTO.ikkeRegistrert()
        log.info("Henter bemanningsforetak $bemanningsforetak")
        return bemanningsforetak
    }

    fun hentBemanningsforetakStatus(organisasjonsnummer: Organisasjonsnummer): RegisterstatusDTO {
        val bemanningsforetak = hentBemanningsforetak(organisasjonsnummer)
        return RegisterstatusDTO(
            registernavn = bemanningsforetak.registernavn,
            statusTekst = bemanningsforetak.godkjenningsstatus,
            status = bemanningsforetak.registerstatus,
        )
    }

    fun lastNedOgLagreRegister() {
        val bemanningsforetaksregisteret = lastNedRegister()
        runBlocking { lagreRegister(bemanningsforetaksregisteret) }
    }

    suspend fun lagreRegister(register: List<BemanningsforetakDTO>) {
        register.forEach {
            valkey.set("${it.organisasjonsnummer}:${it.registernavn}", objectMapper.writeValueAsString(it)).await()
        }
        log.info("Lagret ${register.size} resultater fra bemanningsforetaksregisteret")
    }

    fun lastNedRegister(): List<BemanningsforetakDTO> {
        val url = URI(bemanningsforetakRegisterUrl)
        val registerData = parser.lastNedRegisterData("bemanningsforetaksregisteret", url, httpClient)
        val bemanningsforetaksregisteret = parser.parseRegister(registerData)
        return bemanningsforetaksregisteret
    }
}