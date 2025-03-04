package no.nav.arbeid.registeroppslag.valkey

import glide.api.GlideClient
import glide.api.models.configuration.GlideClientConfiguration
import glide.api.models.configuration.NodeAddress
import glide.api.models.configuration.ServerCredentials
import kotlinx.coroutines.future.await

suspend fun opprettValkeyKlient(env: Map<String, String>): GlideClient {
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
