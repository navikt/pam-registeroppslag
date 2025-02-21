package no.nav.arbeid.registeroppslag.app.test

import no.nav.arbeid.registeroppslag.ApplicationContext
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/*
 * Application context som kan brukes i tester, denne inkluderer en egen mock-oauth2-server
 */
class TestApplicationContext(
    private val localEnv: MutableMap<String, String>,
) : ApplicationContext(localEnv) {

    private val log: Logger = LoggerFactory.getLogger("LocalApplicationContext")

    val mockOauth2Server = MockOAuth2Server().also { server ->
        server.start()

        localEnv["AZURE_APP_WELL_KNOWN_URL"] = server.wellKnownUrl("azuread").toString()
        localEnv["TOKEN_X_WELL_KNOWN_URL"] = server.wellKnownUrl("tokenx").toString()

        log.info("Mock Oauth2 server azuread well known url: ${server.wellKnownUrl("azuread")}")
        log.info("Mock Oauth2 server tokenx well known url: ${server.wellKnownUrl("tokenx")}")
    }
}
