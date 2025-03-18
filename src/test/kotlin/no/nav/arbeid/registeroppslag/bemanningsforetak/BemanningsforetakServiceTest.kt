package no.nav.arbeid.registeroppslag.bemanningsforetak

import io.mockk.every
import kotlinx.coroutines.runBlocking
import no.nav.arbeid.registeroppslag.Registerstatus
import no.nav.arbeid.registeroppslag.RegisterstatusDTO
import no.nav.arbeid.registeroppslag.app.test.TestRunningApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test


class BemanningsforetakServiceTest : TestRunningApplication() {
    val registerdata = this::class.java.getResource("/bemanningsforetaksregister.json")!!.readBytes()
    val bftReg: List<BemanningsforetakDTO> = appCtx.bemanningsforetakParser.parseRegister(registerdata)
    val valkey = appCtx.valkey
    val parserMock = appCtx.bemanningsforetakParserMock
    val bftService = appCtx.bemanningsforetakService

    @AfterEach
    fun ryddOpp() {
        valkey.flushdb()
        assertThat(valkey.dbsize()).isEqualTo(0)
    }

    @Test
    fun `Skal lagre bemanningsforetak`() {
        runBlocking { bftService.lagreRegister(bftReg) }

        val lagretForetak = bftService.hentBemanningsforetak(bftReg.first().organisasjonsnummer)

        assertThat(lagretForetak).isEqualTo(bftReg.first())
    }

    @Test
    fun `Skal hente registrerte bemanningsforetak`() {
        runBlocking { bftService.lagreRegister(bftReg) }
        val forventetHovedenhet = bftReg.first()
        val forventetUnderenhet = bftReg.last()

        val hovedenhet = bftService.hentBemanningsforetak(forventetHovedenhet.organisasjonsnummer)
        val underenhet = bftService.hentBemanningsforetak(forventetUnderenhet.organisasjonsnummer)

        assertThat(hovedenhet).isNotEqualTo(underenhet)
        assertThat(hovedenhet).isEqualTo(forventetHovedenhet)
        assertThat(underenhet).isEqualTo(forventetUnderenhet)
        assertThat(hovedenhet.registerstatus).isEqualTo(Registerstatus.GODKJENT)
        assertThat(underenhet.registerstatus).isEqualTo(Registerstatus.IKKE_GODKJENT)
    }

    @Test
    fun `Skal hente ikke registrert bemanningsforetak`() {
        val ikkeRegistrert = BemanningsforetakDTO.ikkeRegistrert()

        val bemanningsforetak = bftService.hentBemanningsforetak(ikkeRegistrert.organisasjonsnummer)

        assertThat(bemanningsforetak).isEqualTo(ikkeRegistrert)
        assertThat(bemanningsforetak.registerstatus).isEqualTo(Registerstatus.IKKE_REGISTRERT)
    }

    @Test
    fun `Skal hente status til registrerte bemanningsforetak`() {
        runBlocking { bftService.lagreRegister(bftReg) }
        val hovedenhet = bftReg.first()
        val underenhet = bftReg.last()

        val hovedenhetStatus = bftService.hentBemanningsforetakStatus(hovedenhet.organisasjonsnummer)
        val underenhetStatus = bftService.hentBemanningsforetakStatus(underenhet.organisasjonsnummer)

        assertThat(hovedenhetStatus).isNotEqualTo(underenhetStatus)
        assertThat(hovedenhetStatus).isEqualTo(
            RegisterstatusDTO(hovedenhet.registernavn, hovedenhet.godkjenningsstatus, hovedenhet.registerstatus)
        )
        assertThat(underenhetStatus).isEqualTo(
            RegisterstatusDTO(underenhet.registernavn, underenhet.godkjenningsstatus, underenhet.registerstatus)
        )
        assertThat(hovedenhetStatus.status).isEqualTo(Registerstatus.GODKJENT)
        assertThat(underenhetStatus.status).isEqualTo(Registerstatus.IKKE_GODKJENT)
    }

    @Test
    fun `Skal hente status til ikke registrert bemanningsforetak`() {
        val forventet = BemanningsforetakDTO.ikkeRegistrert()

        val bemanningsforetakStatus = bftService.hentBemanningsforetakStatus(forventet.organisasjonsnummer)

        assertThat(bemanningsforetakStatus).isEqualTo(
            RegisterstatusDTO(forventet.registernavn, forventet.godkjenningsstatus, forventet.registerstatus)
        )
        assertThat(bemanningsforetakStatus.status).isEqualTo(Registerstatus.IKKE_REGISTRERT)
    }

    @Test
    fun `Skal laste ned register fra url`() {
        every { parserMock.lastNedRegisterData(any(), any(), any()) } returns registerdata
        every { parserMock.parseRegister(registerdata) } returns bftReg

        val bemanningsforetaksregisteret = bftService.lastNedRegister()

        assertThat(bemanningsforetaksregisteret).isEqualTo(bftReg)
    }

    @Test
    fun `Skal laste ned og lagre register`() {
        every { parserMock.lastNedRegisterData(any(), any(), any()) } returns registerdata
        every { parserMock.parseRegister(registerdata) } returns bftReg

        bftService.lastNedOgLagreRegister()

        val lagretForetak = bftService.hentBemanningsforetak(bftReg.first().organisasjonsnummer)

        assertThat(lagretForetak).isEqualTo(bftReg.first())
    }

    @Test
    fun `Skal overskrive eksisterende bemanningsforetak`() {
        runBlocking { bftService.lagreRegister(bftReg) }
        val nyttForetak = bftReg.first().copy(registerstatus = Registerstatus.IKKE_REGISTRERT)

        runBlocking { bftService.lagreRegister(listOf(nyttForetak)) }

        val lagretForetak = bftService.hentBemanningsforetak(nyttForetak.organisasjonsnummer)

        assertThat(lagretForetak).isEqualTo(nyttForetak)
        assertThat(lagretForetak).isNotEqualTo(bftReg.first())
    }
}