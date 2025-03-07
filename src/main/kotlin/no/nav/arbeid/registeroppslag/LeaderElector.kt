package no.nav.arbeid.registeroppslag

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.net.URI

class LeaderElector(
    private val electorUrl: String,
    private val noLeaderElection: Boolean,
    private val objectMapper: ObjectMapper,
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(LeaderElector::class.java)
    }

    val erLeader: Boolean = InetAddress.getLocalHost().hostName == hentLeader()

    private fun hentLeader(): String {
        if (noLeaderElection) {
            log.info("Ingen leader election, denne noden er leader")
            return InetAddress.getLocalHost().hostName
        }
        val leader: Elector = objectMapper.readValue(URI.create(electorUrl).toURL(), Elector::class.java)
        log.info("Leader er $leader")
        return leader.toString()
    }
}

@JvmInline
value class Elector(private val name: String)