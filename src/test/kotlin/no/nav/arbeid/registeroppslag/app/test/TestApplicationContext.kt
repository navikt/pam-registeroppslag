package no.nav.arbeid.registeroppslag.app.test

import io.mockk.mockk
import no.nav.arbeid.registeroppslag.ApplicationContext
import no.nav.arbeid.registeroppslag.app.test.TestRunningApplication.Companion.appCtx
import no.nav.arbeid.registeroppslag.bemanningsforetak.BemanningsforetakController
import no.nav.arbeid.registeroppslag.bemanningsforetak.BemanningsforetakParser
import no.nav.arbeid.registeroppslag.bemanningsforetak.BemanningsforetakService
import no.nav.arbeid.registeroppslag.renholdsvirksomhet.RenholdController
import no.nav.arbeid.registeroppslag.renholdsvirksomhet.RenholdParser
import no.nav.arbeid.registeroppslag.renholdsvirksomhet.RenholdService
import no.nav.arbeid.registeroppslag.scheduler.Scheduler
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

/*
 * Application context som kan brukes i tester, denne inkluderer en egen mock-oauth2-server
 */
class TestApplicationContext(
    private val localEnv: MutableMap<String, String>,
    val localValKey: GenericContainer<*> = GenericContainer(DockerImageName.parse("valkey/valkey:8.0-alpine"))
        .waitingFor(Wait.forListeningPort())
        .apply {
            withExposedPorts(6379)
            start()
        }
        .also { localConfig ->
            localEnv["VALKEY_HOST_REGISTEROPPSLAG"] = localConfig.host
            localEnv["VALKEY_PORT_REGISTEROPPSLAG"] = localConfig.getMappedPort(6379).toString()
        }
) : ApplicationContext(localEnv) {

    private val log: Logger = LoggerFactory.getLogger("LocalApplicationContext")

    override val scheduler: Scheduler = Scheduler {
        Scheduler.log.info("Kjører i testmodus, scheduler kjøres ikke")
        appCtx.scheduler.stop()
    }

    val bemanningsforetakParserMock = mockk<BemanningsforetakParser>()
    val bemanningsforetakServiceMock = mockk<BemanningsforetakService>()
    override val bemanningsforetakService = BemanningsforetakService(
        parser = bemanningsforetakParserMock,
        httpClient = httpClient,
        valkey = valkey,
        objectMapper = objectMapper,
        metrikker = metrikker,
        bemanningsforetakRegisterUrl = "http://localhost"
    )
    override val bemanningsforetakController = BemanningsforetakController(bemanningsforetakServiceMock)

    val renholdParserMock = mockk<RenholdParser>()
    val renholdServiceMock = mockk<RenholdService>()
    override val renholdService = RenholdService(
        parser = renholdParserMock,
        httpClient = httpClient,
        valkey = valkey,
        objectMapper = objectMapper,
        metrikker = metrikker,
        renholdsregisterURL = "http://localhost"
    )
    override val renholdController = RenholdController(renholdServiceMock)

    val mockOauth2Server = MockOAuth2Server().also { server ->
        server.start()

        localEnv["AZURE_APP_WELL_KNOWN_URL"] = server.wellKnownUrl("azuread").toString()
        localEnv["TOKEN_X_WELL_KNOWN_URL"] = server.wellKnownUrl("tokenx").toString()

        log.info("Mock Oauth2 server azuread well known url: ${server.wellKnownUrl("azuread")}")
        log.info("Mock Oauth2 server tokenx well known url: ${server.wellKnownUrl("tokenx")}")
    }

    fun opprettToken(): String {
        val token = appCtx.mockOauth2Server.issueToken(
            issuerId = appCtx.env.getValue("TOKEN_X_ISSUER"),
            audience = appCtx.env.getValue("TOKEN_X_CLIENT_ID"),
            expiry = 3600L
        )
        return "Bearer ${token.serialize()}"
    }
}
