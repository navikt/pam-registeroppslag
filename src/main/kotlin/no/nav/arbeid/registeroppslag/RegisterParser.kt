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

    fun lastNedRegisterData(registerNavn: String, registerUrl: URI, httpClient: HttpClient): ByteArray {
        val request = HttpRequest.newBuilder(registerUrl)
            .header("Accept", "application/json")
            .GET()
            .build()

        log.info("Laster ned $registerNavn fra $registerUrl")
        val response = httpClient.send(request, BodyHandlers.ofString())
        log.info("Mottok: ${response.body()}")
        return response.body().toByteArray()
    }
}
