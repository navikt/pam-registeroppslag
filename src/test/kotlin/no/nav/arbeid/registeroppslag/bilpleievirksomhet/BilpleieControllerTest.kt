package no.nav.arbeid.registeroppslag.bilpleievirksomhet

import io.javalin.http.HttpStatus
import io.mockk.every
import no.nav.arbeid.registeroppslag.app.test.TestRunningApplication
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jetty.http.HttpHeader
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

class BilpleieControllerTest : TestRunningApplication() {
    val virksomhet = appCtx.objectMapper.readValue(
        this::class.java.getResource("/bilpleievirksomhet.json"),
        BilpleievirksomhetDTO::class.java
    )
    val bilpleieServiceMock = appCtx.bilpleieServiceMock

    @Test
    fun `Skal feile uten gyldig token`() {
        val request = HttpRequest.newBuilder()
            .uri(URI("$lokalUrlBase/api/bilpleievirksomhet/${virksomhet.organisasjonsnummer}"))
            .GET()
            .build()
        val response = appCtx.httpClient.send(request, BodyHandlers.ofString())

        assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.code)
        assertThat(response.body()).isNotEqualTo(appCtx.objectMapper.writeValueAsString(virksomhet))
    }

    @Test
    fun `Skal håndtere BadRequest ved ugyldig organisasjonsnummer`() {
        val request = lagRequest("bilpleievirksomhet/ikkeetgyldigorgnr")
        val response = appCtx.httpClient.send(request, BodyHandlers.ofString())

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.code)
        assertThat(response.body()).isEqualTo("Ugyldig organisasjonsnummer")
    }

    @Test
    fun `Skal hente bilpleievirksomhet`() {
        every { bilpleieServiceMock.hentBilpleievirksomhet(virksomhet.organisasjonsnummer) } returns virksomhet

        val request = lagRequest("bilpleievirksomhet/${virksomhet.organisasjonsnummer}")
        val response = appCtx.httpClient.send(request, BodyHandlers.ofString())

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.code)
        assertThat(response.body()).isEqualTo(appCtx.objectMapper.writeValueAsString(virksomhet))
    }

    @Test
    fun `Skal håndtere at virksomheten ikke er registrert`() {
        every { bilpleieServiceMock.hentBilpleievirksomhet(virksomhet.organisasjonsnummer) } returns BilpleievirksomhetDTO.ikkeRegistrert()

        val request = lagRequest("bilpleievirksomhet/${virksomhet.organisasjonsnummer}")
        val response = appCtx.httpClient.send(request, BodyHandlers.ofString())

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.code)
        assertThat(response.body()).isEqualTo(appCtx.objectMapper.writeValueAsString(BilpleievirksomhetDTO.ikkeRegistrert()))
    }

    @Test
    fun `Skal hente status for en registrert bilpleievirksomhet`() {
        every { bilpleieServiceMock.hentBilpleievirksomhetStatus(virksomhet.organisasjonsnummer) } returns virksomhet.tilRegisterstatus()

        val request = lagRequest("bilpleievirksomhet/${virksomhet.organisasjonsnummer}/status")
        val response = appCtx.httpClient.send(request, BodyHandlers.ofString())

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.code)
        assertThat(response.body()).isEqualTo(appCtx.objectMapper.writeValueAsString(virksomhet.tilRegisterstatus()))
    }

    @Test
    fun `Skal laste ned og lagre register`() {
        every { bilpleieServiceMock.lastNedOgLagreRegister() } returns Unit

        val request = lagRequest("bilpleieregister/lastned")
        val response = appCtx.httpClient.send(request, BodyHandlers.ofString())

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.code)
        assertThat(response.body()).isEqualTo("Lastet ned og lagret ${BilpleievirksomhetDTO.registernavn}")
    }

    private fun lagRequest(path: String): HttpRequest {
        return HttpRequest.newBuilder()
            .uri(URI("$lokalUrlBase/api/$path"))
            .header(HttpHeader.AUTHORIZATION.name, appCtx.opprettToken())
            .GET()
            .build()
    }
}
