package no.nav.arbeid.registeroppslag.app.local

import no.nav.arbeid.registeroppslag.ApplicationContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

/*
 * Application context som kan brukes til å starte appen lokalt, her kan det f.eks. settes opp en lokal postgres og kafka.
 */
class LocalApplicationContext(
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
) : ApplicationContext(localEnv)