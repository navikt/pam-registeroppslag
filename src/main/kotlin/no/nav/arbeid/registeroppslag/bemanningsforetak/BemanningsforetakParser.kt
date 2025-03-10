package no.nav.arbeid.registeroppslag.bemanningsforetak

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeid.registeroppslag.RegisterParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BemanningsforetakParser(
    private val objectMapper: ObjectMapper,
) : RegisterParser {
    companion object {
        val log: Logger = LoggerFactory.getLogger(BemanningsforetakParser::class.java)
    }

    /**
     * Parser bemaningsforetaksregisteret og returnerer en liste med hoved- og underenheter fra registeret
     * slik at man kan slå opp på både hoved- og underenheter.
     *
     * @param register ByteArray med registerdata
     * @return List<BemanningsforetakDTO> med hoved- og underenheter i registeret
     */
    override fun parseRegister(register: ByteArray): List<BemanningsforetakDTO> {
        val bemanningsforetaksregister = objectMapper.readValue(register, BemanningsforetaksregisterDTO::class.java)
        val registerParsed: MutableList<BemanningsforetakDTO> = mutableListOf()

        log.info("Parser hoved- og underenheter i bemanningsforetaksregisteret")
        bemanningsforetaksregister.data.forEach { hovedenhet ->
            registerParsed.add(hovedenhet)
            hovedenhet.underenheter?.let { underenheter ->
                registerParsed.addAll(underenheter.map { it })
            }
        }
        log.info("Parset ${registerParsed.size} enheter")
        return registerParsed
    }
}