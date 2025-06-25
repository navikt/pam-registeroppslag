package no.nav.arbeid.registeroppslag.bilpleievirksomhet

import io.mockk.every
import no.nav.arbeid.registeroppslag.app.test.TestRunningApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class BilpleieServiceTest : TestRunningApplication() {
    val bilpleieregisterdata = this::class.java.getResource("/bilpleieregister.json")!!.readBytes()
    val bilverksteddata = this::class.java.getResource("/verksted_testdata.csv")!!.readBytes()
    val bilpleievirksomheter: List<BilpleievirksomhetDTO> = appCtx.bilpleieParser.parseRegister(bilpleieregisterdata)
    val bilverksteder: List<BilpleievirksomhetDTO> = appCtx.bilpleieParser.parseBilverksted(bilverksteddata)
    val bilpleieregister = bilpleievirksomheter + bilverksteder
    val registernavn = BilpleievirksomhetDTO.registernavn
    val valkey = appCtx.valkey
    val parserMock = appCtx.bilpleieParserMock
    val bilpleieService = appCtx.bilpleieService

    @AfterEach
    fun ryddOpp() {
        valkey.flushdb()
        assertThat(valkey.dbsize()).isEqualTo(0)
    }

    @Test
    fun `Skal lagre bilpleievirksomhet`() {
        bilpleieService.lagreRegister(bilpleieregister)

        val lagredeVirksomheter = bilpleieregister.map { bilpleieService.hentBilpleievirksomhet(it.organisasjonsnummer) }

        assertThat(lagredeVirksomheter).containsExactlyElementsOf(bilpleieregister)
    }

    @Test
    fun `Skal hente registrerte bilpleievirksomheter`() {
        bilpleieService.lagreRegister(bilpleieregister)

        val registrerteVirksomheter = bilpleieregister.map { bilpleieService.hentBilpleievirksomhet(it.organisasjonsnummer) }

        assertThat(registrerteVirksomheter).containsExactlyElementsOf(bilpleieregister)
    }

    @Test
    fun `Skal hente ikke registrert bilpleievirksomhet`() {
        val ikkeRegistrert = BilpleievirksomhetDTO.ikkeRegistrert()

        val bilpleievirksomhet = bilpleieService.hentBilpleievirksomhet(ikkeRegistrert.organisasjonsnummer)

        assertThat(bilpleievirksomhet).isEqualTo(ikkeRegistrert)
    }

    @Test
    fun `Skal hente status til registrerte bilpleievirksomheter`() {
        bilpleieService.lagreRegister(bilpleieregister)

        val registrerteVirksomheter = bilpleieregister.map { bilpleieService.hentBilpleievirksomhetStatus(it.organisasjonsnummer) }

        assertThat(registrerteVirksomheter).containsExactlyElementsOf(bilpleieregister.map { it.tilRegisterstatus() })
    }

    @Test
    fun `Skal hente status til ikke registrert bilpleievirksomhet`() {
        val ikkeRegistrert = BilpleievirksomhetDTO.ikkeRegistrert()

        val registerstatus = bilpleieService.hentBilpleievirksomhetStatus(ikkeRegistrert.organisasjonsnummer)

        assertThat(registerstatus).isEqualTo(ikkeRegistrert.tilRegisterstatus())
    }

    @Test
    fun `Skal laste ned og lagre register`() {
        every { parserMock.lastNedRegisterData(registernavn, any(), any(), any()) } returns bilpleieregisterdata
        every { parserMock.lastNedRegisterData("bilverksted", any(), any(), any()) } returns bilverksteddata
        every { parserMock.parseRegister(any()) } returns bilpleievirksomheter
        every { parserMock.parseBilverksted(any()) } returns bilverksteder

        bilpleieService.lastNedOgLagreRegister()

        val lagredeVirksomheter = bilpleieregister.map { bilpleieService.hentBilpleievirksomhet(it.organisasjonsnummer) }

        assertThat(lagredeVirksomheter).containsExactlyElementsOf(bilpleieregister)
    }
}
