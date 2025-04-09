package no.nav.arbeid.registeroppslag

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

interface RegisterParser {
    companion object {
        val log: Logger = LoggerFactory.getLogger(RegisterParser::class.java)
    }

    fun parseRegister(register: ByteArray): List<Any>

    fun lastNedRegisterData(registerNavn: String, registerUrl: URI, httpClient: HttpClient, headers: Map<String, String> = emptyMap()): ByteArray {
        val requestBuilder = HttpRequest.newBuilder(registerUrl)
            .header("Accept", "application/json")
            .GET()

        if (headers.isNotEmpty())    {
            headers.forEach { (key, value) ->
                requestBuilder.header(key, value)
            }
        }

        log.info("Laster ned $registerNavn fra $registerUrl")
        val response = httpClient.send(requestBuilder.build(), BodyHandlers.ofByteArray())
        return when (response.statusCode()) {
            200 -> response.body()
            else -> throw RuntimeException("Feil ved forespørsel mot $registerNavn, ${response.statusCode()}: ${response.body().decodeToString()}")
        }
    }
}
