package no.nav.arbeid.registeroppslag.renholdsvirksomhet

import io.mockk.every
import no.nav.arbeid.registeroppslag.app.test.TestRunningApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class RenholdServiceTest : TestRunningApplication() {
    val registerdata = this::class.java.getResource("/renholdsregister.xml")!!.readBytes()
    val renholdsregister: List<RenholdsvirksomhetDTO> = appCtx.renholdParser.parseRegister(registerdata)
    val registernavn = RenholdsvirksomhetDTO.registernavn
    val valkey = appCtx.valkey
    val parserMock = appCtx.renholdParserMock
    val renholdService = appCtx.renholdService

    @AfterEach
    fun ryddOpp() {
        valkey.flushdb()
        assertThat(valkey.dbsize()).isEqualTo(0)
    }

    @Test
    fun `Skal lagre renholdsvirksomhet`() {
        renholdService.lagreRegister(renholdsregister)

        val lagretVirksomhet = renholdService.hentRenholdsvirksomhet(renholdsregister.first().organisasjonsnummer)

        assertThat(lagretVirksomhet).isEqualTo(renholdsregister.first())
    }

    @Test
    fun `Skal hente registrerte renholdsvirksomheter`() {
        renholdService.lagreRegister(renholdsregister)

        val registrerteVirksomheter = renholdsregister.map { renholdService.hentRenholdsvirksomhet(it.organisasjonsnummer) }

        assertThat(registrerteVirksomheter).containsExactlyInAnyOrderElementsOf(renholdsregister)
    }

    @Test
    fun `Skal hente ikke registrert renholdsvirksomhet`() {
        val ikkeRegistrert = RenholdsvirksomhetDTO.ikkeRegistrert()

        val renholdsvirksomhet = renholdService.hentRenholdsvirksomhet(ikkeRegistrert.organisasjonsnummer)

        assertThat(renholdsvirksomhet).isEqualTo(ikkeRegistrert)
    }

    @Test
    fun `Skal hente status til registrerte renholdsvirksomheter`() {
        renholdService.lagreRegister(renholdsregister)

        val registrerteVirksomheter = renholdsregister.map { renholdService.hentRenholdsvirksomhetStatus(it.organisasjonsnummer) }

        assertThat(registrerteVirksomheter).containsExactlyInAnyOrderElementsOf(renholdsregister.map { it.tilRegisterstatus() })
    }

    @Test
    fun `Skal hente status til ikke registrert renholdsvirksomhet`() {
        val ikkeRegistrert = RenholdsvirksomhetDTO.ikkeRegistrert()

        val registerstatus = renholdService.hentRenholdsvirksomhetStatus(ikkeRegistrert.organisasjonsnummer)

        assertThat(registerstatus).isEqualTo(ikkeRegistrert.tilRegisterstatus())
    }

    @Test
    fun `Skal laste ned og lagre register`() {
        every { parserMock.lastNedRegisterData(registernavn, any(), any()) } returns registerdata
        every { parserMock.parseRegister(registerdata) } returns renholdsregister

        renholdService.lastNedOgLagreRegister()

        val lagredeVirksomheter = renholdsregister.map { renholdService.hentRenholdsvirksomhet(it.organisasjonsnummer) }

        assertThat(lagredeVirksomheter).containsExactlyInAnyOrderElementsOf(renholdsregister)
    }

    @Test
    fun `Skal overskrive eksisterende virksomheter`() {
        renholdService.lagreRegister(renholdsregister)
        val nyVirksomhet = renholdsregister.first().copy(navn = "Oppdatert navn", godkjenningsstatus = "Ikke godkjent")

        renholdService.lagreRegister(listOf(nyVirksomhet))
        val lagredeVirksomheter = renholdsregister.map { renholdService.hentRenholdsvirksomhet(it.organisasjonsnummer) }

        assertThat(lagredeVirksomheter).doesNotContain(renholdsregister.first())
        assertThat(lagredeVirksomheter).containsExactlyInAnyOrderElementsOf(renholdsregister.drop(1) + nyVirksomhet)
    }
}