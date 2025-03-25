package no.nav.arbeid.registeroppslag.renholdsvirksomhet

import io.javalin.http.HttpStatus
import io.mockk.every
import no.nav.arbeid.registeroppslag.app.test.TestRunningApplication
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jetty.http.HttpHeader
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

class RenholdControllerTest : TestRunningApplication() {
    val virksomhet = appCtx.objectMapper.readValue(
        this::class.java.getResource("/renholdsvirksomhet.json"),
        RenholdsvirksomhetDTO::class.java
    )
    val renholdServiceMock = appCtx.renholdServiceMock

    @Test
    fun `Skal feile uten riktig token`() {
        every { renholdServiceMock.hentRenholdsvirksomhet(virksomhet.organisasjonsnummer) } returns virksomhet

        val request = HttpRequest.newBuilder()
            .uri(URI("$lokalUrlBase/api/renholdsvirksomhet/${virksomhet.organisasjonsnummer}"))
            .GET()
            .build()

        val response = appCtx.httpClient.send(request, BodyHandlers.ofString())

        assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.code)
        assertThat(response.body()).isNotEqualTo(appCtx.objectMapper.writeValueAsString(virksomhet))
    }

    @Test
    fun `Skal håndtere BadRequest ved ugyldig organisasjonsnummer`() {
        val request = lagRequest("ikkeetgyldigorgnr")
        val response = appCtx.httpClient.send(request, BodyHandlers.ofString())

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.code)
        assertThat(response.body()).isEqualTo("Ugyldig organisasjonsnummer")
    }

    @Test
    fun `Skal hente renholdsvirksomhet`() {
        every { renholdServiceMock.hentRenholdsvirksomhet(virksomhet.organisasjonsnummer) } returns virksomhet

        val request = lagRequest("${virksomhet.organisasjonsnummer}")
        val response = appCtx.httpClient.send(request, BodyHandlers.ofString())

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.code)
        assertThat(response.body()).isEqualTo(appCtx.objectMapper.writeValueAsString(virksomhet))
    }

    @Test
    fun `Skal håndtere at virksomheten ikke er registrert`() {
        every { renholdServiceMock.hentRenholdsvirksomhet(virksomhet.organisasjonsnummer) } returns RenholdsvirksomhetDTO.ikkeRegistrert()

        val request = lagRequest("${virksomhet.organisasjonsnummer}")
        val response = appCtx.httpClient.send(request, BodyHandlers.ofString())

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.code)
        assertThat(response.body()).isEqualTo(appCtx.objectMapper.writeValueAsString(RenholdsvirksomhetDTO.ikkeRegistrert()))
    }

    @Test
    fun `Skal hente status for renholdsvirksomhet`() {
        every { renholdServiceMock.hentRenholdsvirksomhetStatus(virksomhet.organisasjonsnummer) } returns virksomhet.tilRegisterstatus()

        val request = lagRequest("${virksomhet.organisasjonsnummer}/status")
        val response = appCtx.httpClient.send(request, BodyHandlers.ofString())

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.code)
        assertThat(response.body()).isEqualTo(appCtx.objectMapper.writeValueAsString(virksomhet.tilRegisterstatus()))
    }

    @Test
    fun `Skal laste ned og lagre register`() {
        every { renholdServiceMock.lastNedOgLagreRegister() } returns Unit

        val request = lagRequest("lastned")
        val response = appCtx.httpClient.send(request, BodyHandlers.ofString())

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.code)
        assertThat(response.body()).isEqualTo("Lastet ned og lagret ${RenholdsvirksomhetDTO.registernavn}")
    }

    private fun lagRequest(path: String) = HttpRequest.newBuilder()
        .uri(URI("$lokalUrlBase/api/renholdsvirksomhet/$path"))
        .header(HttpHeader.AUTHORIZATION.name, appCtx.opprettToken())
        .GET()
        .build()
}