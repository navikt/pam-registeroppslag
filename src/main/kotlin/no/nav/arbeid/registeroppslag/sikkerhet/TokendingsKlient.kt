package no.nav.arbeid.registeroppslag.sikkerhet

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.arbeid.registeroppslag.common.retryTemplate
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.*


class TokendingsKlient(
    private val tokenDingsExchangeUrl: String,
    private val privateJwk: String,
    private val clientId: String,
    private val issuer: String,
    private val httpClient: HttpClient
) {
    companion object {
        private val log = LoggerFactory.getLogger(TokendingsKlient::class.java)
        private val objectMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setTimeZone(TimeZone.getTimeZone("Europe/Oslo"))
    }

    fun veksleInnToken(accessToken: String, audience: String): String {
        val formData = mapOf(
            "grant_type" to "urn:ietf:params:oauth:grant-type:token-exchange",
            "client_assertion" to getClientAssertion(
                TokenXProperties(
                    clientId,
                    issuer,
                    privateJwk,
                    tokenDingsExchangeUrl
                )
            ),
            "client_assertion_type" to "urn:ietf:params:oauth:client-assertion-type:jwt-bearer",
            "subject_token_type" to "urn:ietf:params:oauth:token-type:jwt",
            "audience" to audience,
            "subject_token" to accessToken,
        )

        val request = HttpRequest.newBuilder()
            .uri(URI(tokenDingsExchangeUrl))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .timeout(Duration.ofSeconds(5))
            .POST(HttpRequest.BodyPublishers.ofString(getFormDataAsString(formData)))
            .build()

        val response = retryTemplate(logg = log, requestUrl = tokenDingsExchangeUrl) {
            httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            )
        }

        if (response.statusCode() >= 300 || response.body() == null) {
            log.error("Greide ikke å veksle inn token ${response.statusCode()} : ${response.body()}")
            throw RuntimeException("unknown error (responseCode=${response.statusCode()}) ved veksling av token")
        }

        val token = objectMapper.readValue(response.body(), ExchangeToken::class.java)
        return token.accessToken
    }

    fun getClientAssertion(properties: TokenXProperties): String? {
        val claimsSet: JWTClaimsSet = JWTClaimsSet.Builder()
            .subject(properties.clientId)
            .issuer(properties.clientId)
            .audience(properties.tokenEndpoint)
            .issueTime(Date())
            .notBeforeTime(Date())
            .expirationTime(Date(Date().getTime() + 120 * 1000))
            .jwtID(UUID.randomUUID().toString())
            .build()
        val signedJWT = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(properties.parseJwk().keyID)
                .build(),
            claimsSet
        )
        try {
            signedJWT.sign(properties.getJwsSigner())
        } catch (e: JOSEException) {
            throw RuntimeException(e)
        }
        return signedJWT.serialize()
    }

    private fun getFormDataAsString(f: Map<String, String?>): String {
        val params = mutableListOf<String>()
        f.forEach { d ->
            val key = URLEncoder.encode(d.key, "UTF-8")
            val value = URLEncoder.encode(d.value, "UTF-8")
            params.add("${key}=${value}")
        }
        return params.joinToString("&")
    }

    data class TokenXProperties(
        val clientId: String,
        val issuer: String,
        val privateJwk: String,
        val tokenEndpoint: String,
    ) {
        fun parseJwk() = RSAKey.parse(privateJwk)
        fun getJwsSigner() = RSASSASigner(parseJwk())
    }

    data class ExchangeToken(
        @JsonAlias("access_token")
        val accessToken: String,
        @JsonAlias("expires_in")
        val expiresIn: Int,
    )
}
