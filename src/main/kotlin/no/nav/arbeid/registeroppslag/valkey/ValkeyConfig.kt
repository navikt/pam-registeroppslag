package no.nav.arbeid.registeroppslag.valkey

import glide.api.BaseClient
import glide.api.GlideClient
import glide.api.GlideClusterClient
import glide.api.models.configuration.GlideClientConfiguration
import glide.api.models.configuration.GlideClusterClientConfiguration
import glide.api.models.configuration.NodeAddress
import glide.api.models.configuration.ServerCredentials
import kotlinx.coroutines.future.await
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log: Logger = LoggerFactory.getLogger("no.nav.arbeid.registeroppslag.valkey.ValkeyConfig")

private suspend fun opprettEnkelValkeyKlient(env: Map<String, String>): GlideClient {
    val config = GlideClientConfiguration.builder()
        .addresses(listOf(NodeAddress.builder()
            .host(env.getValue("VALKEY_HOST_REGISTEROPPSLAG"))
            .port(env.getValue("VALKEY_PORT_REGISTEROPPSLAG").toInt())
            .build()
        ))
        .credentials(ServerCredentials.builder()
            .username(env.getValue("VALKEY_USERNAME_REGISTEROPPSLAG"))
            .password(env.getValue("VALKEY_PASSWORD_REGISTEROPPSLAG"))
            .build()
        )
        .useTLS(env.getValue("VALKEY_USE_TLS").toBoolean())
        .build()

    return GlideClient.createClient(config).await()
}

private suspend fun opprettClusterValkeyKlient(env: Map<String, String>): GlideClusterClient {
    val config = GlideClusterClientConfiguration.builder()
        .addresses(listOf(NodeAddress.builder()
            .host(env.getValue("VALKEY_HOST_REGISTEROPPSLAG"))
            .port(env.getValue("VALKEY_PORT_REGISTEROPPSLAG").toInt())
            .build()
        ))
        .credentials(ServerCredentials.builder()
            .username(env.getValue("VALKEY_USERNAME_REGISTEROPPSLAG"))
            .password(env.getValue("VALKEY_PASSWORD_REGISTEROPPSLAG"))
            .build()
        )
        .clientName("cluster_client")
        .useTLS(env.getValue("VALKEY_USE_TLS").toBoolean())
        .build()

    return GlideClusterClient.createClient(config).await()
}

suspend fun opprettValkeyKlient(env: Map<String, String>): BaseClient {
    return try {
        log.info("Oppretter cluster valkey klient")
        opprettClusterValkeyKlient(env)
    } catch (e: Exception) {
        try {
            log.warn("Feil ved oppretting av cluster valkey klient, forsøker enkel klient", e)
            opprettEnkelValkeyKlient(env)
        } catch (e: Exception) {
            log.error("Oppretting av valkey klient feilet", e)
            throw e
        }
    }
}