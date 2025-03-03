package no.nav.arbeid.registeroppslag.valkey

import glide.api.GlideClient
import glide.api.models.configuration.GlideClientConfiguration
import glide.api.models.configuration.NodeAddress
import kotlinx.coroutines.future.await

suspend fun opprettValkeyKlient(host: String, port: Int): GlideClient {
    val config = GlideClientConfiguration.builder()
        .addresses(listOf(NodeAddress.builder().host(host).port(port).build()))
        .build()

    return GlideClient.createClient(config).await()
}