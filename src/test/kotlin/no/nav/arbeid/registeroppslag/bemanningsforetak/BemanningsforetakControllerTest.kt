package no.nav.arbeid.registeroppslag.bemanningsforetak

import io.javalin.http.HttpStatus
import io.mockk.every
import no.nav.arbeid.registeroppslag.RegisterstatusDTO
import no.nav.arbeid.registeroppslag.app.test.TestRunningApplication
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jetty.http.HttpHeader
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

class BemanningsforetakControllerTest : TestRunningApplication() {
    val foretak = appCtx.objectMapper.readValue(
        this::class.java.getResource("/bemanningsforetak.json"),
        BemanningsforetakDTO::class.java
    )
    val bftServiceMock = appCtx.bemanningsforetakServiceMock

    @Test
    fun `Skal feile uten riktig token`() {
        every { bftServiceMock.hentBemanningsforetak(foretak.organisasjonsnummer) } returns foretak

        val request = HttpRequest.newBuilder()
            .uri(URI("$lokalUrlBase/api/bemanningsforetak/${foretak.organisasjonsnummer}"))
            .GET()
            .build()

        val response = appCtx.httpClient.send(request, BodyHandlers.ofString())

        assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.code)
        assertThat(response.body()).isNotEqualTo(appCtx.objectMapper.writeValueAsString(foretak))
    }

    @Test
    fun `Skal hente bemanningsforetak`() {
        every { bftServiceMock.hentBemanningsforetak(foretak.organisasjonsnummer) } returns foretak

        val request = HttpRequest.newBuilder()
            .uri(URI("$lokalUrlBase/api/bemanningsforetak/${foretak.organisasjonsnummer}"))
            .header(HttpHeader.AUTHORIZATION.name, appCtx.opprettToken())
            .GET()
            .build()

        val response = appCtx.httpClient.send(request, BodyHandlers.ofString())

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.code)
        assertThat(response.body()).isEqualTo(appCtx.objectMapper.writeValueAsString(foretak))
    }

    @Test
    fun `Skal håndtere at foretaket ikke er registrert`() {
        every { bftServiceMock.hentBemanningsforetak(foretak.organisasjonsnummer) } returns BemanningsforetakDTO.ikkeRegistrert()

        val request = HttpRequest.newBuilder()
            .uri(URI("$lokalUrlBase/api/bemanningsforetak/${foretak.organisasjonsnummer}"))
            .header(HttpHeader.AUTHORIZATION.name, appCtx.opprettToken())
            .GET()
            .build()

        val response = appCtx.httpClient.send(request, BodyHandlers.ofString())

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.code)
        assertThat(response.body()).isEqualTo(appCtx.objectMapper.writeValueAsString(BemanningsforetakDTO.ikkeRegistrert()))
    }

    @Test
    fun `Skal returnere BadRequest ved ugyldig organisasjonsnummer`() {
        val request = HttpRequest.newBuilder()
            .uri(URI("$lokalUrlBase/api/bemanningsforetak/ikkeetgyldigorgnr"))
            .header(HttpHeader.AUTHORIZATION.name, appCtx.opprettToken())
            .GET()
            .build()

        val response = appCtx.httpClient.send(request, BodyHandlers.ofString())

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.code)
        assertThat(response.body()).isEqualTo("Ugyldig organisasjonsnummer")
    }

    @Test
    fun `Skal hente status for bemanningsforetak`() {
        val godkjentForetak = RegisterstatusDTO(BemanningsforetakDTO.registernavn, foretak.godkjenningsstatus, foretak.registerstatus)
        every { bftServiceMock.hentBemanningsforetakStatus(foretak.organisasjonsnummer) } returns godkjentForetak

        val request = HttpRequest.newBuilder()
            .uri(URI("$lokalUrlBase/api/bemanningsforetak/${foretak.organisasjonsnummer}/status"))
            .header(HttpHeader.AUTHORIZATION.name, appCtx.opprettToken())
            .GET()
            .build()

        val response = appCtx.httpClient.send(request, BodyHandlers.ofString())

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.code)
        assertThat(response.body()).isEqualTo(appCtx.objectMapper.writeValueAsString(godkjentForetak))
    }

    @Test
    fun `Skal laste ned og lagre register`() {
        every { bftServiceMock.lastNedOgLagreRegister() } returns Unit

        val request = HttpRequest.newBuilder()
            .uri(URI("$lokalUrlBase/api/bemanningsforetak/lastned"))
            .header(HttpHeader.AUTHORIZATION.name, appCtx.opprettToken())
            .GET()
            .build()

        val response = appCtx.httpClient.send(request, BodyHandlers.ofString())

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.code)
        assertThat(response.body()).isEqualTo("Lastet ned og lagret bemanningsforetaksregisteret")
    }
}

