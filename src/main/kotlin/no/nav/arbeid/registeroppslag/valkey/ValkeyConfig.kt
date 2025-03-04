package no.nav.arbeid.registeroppslag.valkey

import glide.api.BaseClient
import glide.api.GlideClient
import glide.api.GlideClusterClient
import glide.api.models.configuration.GlideClientConfiguration
import glide.api.models.configuration.GlideClusterClientConfiguration
import glide.api.models.configuration.NodeAddress
import kotlinx.coroutines.future.await
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private suspend fun opprettEnkelValkeyKlient(host: String, port: Int): GlideClient {
    val config = GlideClientConfiguration.builder()
        .addresses(listOf(NodeAddress.builder().host(host).port(port).build()))
        .build()

    return GlideClient.createClient(config).await()
}

private suspend fun opprettClusterValkeyKlient(nodesList: List<Pair<String, Int>>): GlideClusterClient {
    val config = GlideClusterClientConfiguration.builder()
        .addresses(nodesList.map { (host: String, port: Int) -> NodeAddress.builder().host(host).port(port).build() })
        .clientName("cluster_client")
        .build()

    return GlideClusterClient.createClient(config).await()
}

suspend fun opprettValkeyKlient(host: String, port: Int): BaseClient {
    val log: Logger = LoggerFactory.getLogger("no.nav.arbeid.registeroppslag.valkey.ValkeyConfig")

    return try {
        log.info("Oppretter valkey-klient mot cluster")
        opprettClusterValkeyKlient(listOf(Pair(host, port)))
    } catch (e: Exception) {
        try {
            log.warn("Feil under opprettelse av cluster-valkey-klient, forsøker å opprette enkel valkey-klient")
            opprettEnkelValkeyKlient(host, port)
        } catch (e: Exception) {
            log.error("Opprettelse av valkey-klient feilet", e)
            throw RuntimeException("Feil under opprettelse av valkey-klient", e)
        }
    }
}