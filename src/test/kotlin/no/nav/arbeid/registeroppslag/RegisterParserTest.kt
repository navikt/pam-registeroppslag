package no.nav.arbeid.registeroppslag

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpResponse.BodyHandlers


class RegisterParserTest {
    object registerParser : RegisterParser {
        override fun parseRegister(register: ByteArray): List<Any> = emptyList()
    }

    val httpClientMock = mockk<HttpClient>()

    @Test
    fun `Skal laste ned registerdata`() {
        every { httpClientMock.send(any(), BodyHandlers.ofByteArray()) } returns mockk {
            every { statusCode() } returns 200
            every { body() } returns "registerdata".toByteArray()
        }

        val register = registerParser.lastNedRegisterData("register", URI("http://localhost"), httpClientMock)
        assertThat(register).isEqualTo("registerdata".toByteArray())
    }

    @Test
    fun `Skal håndtere feil ved nedlasting av registerdata`() {
        every { httpClientMock.send(any(), BodyHandlers.ofByteArray()) } returns mockk {
            every { statusCode() } returns 404
            every { body() } returns "Not found".toByteArray()
        }

        assertThrows<RuntimeException> {
            registerParser.lastNedRegisterData("register", URI("http://localhost"), httpClientMock)
        }.also {
            assertThat(it.message).isEqualTo("Feil ved forespørsel mot register, 404: Not found")
        }
    }

}