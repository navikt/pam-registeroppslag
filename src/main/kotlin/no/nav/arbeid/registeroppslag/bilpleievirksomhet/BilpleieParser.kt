package no.nav.arbeid.registeroppslag.bilpleievirksomhet

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeid.registeroppslag.RegisterParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BilpleieParser(
    private val objectMapper: ObjectMapper,
) : RegisterParser {
    companion object {
        val log: Logger = LoggerFactory.getLogger(BilpleieParser::class.java)
    }

    /**
     * Parser bilpleieregisteret og returnerer en liste med hoved- og underenheter fra registeret
     * slik at man kan slå opp på både hoved- og underenheter.
     *
     * @param register ByteArray med registerdata
     * @return List<BilpleievirksomhetDTO> med hoved- og underenheter i registeret
     */
    override fun parseRegister(register: ByteArray): List<BilpleievirksomhetDTO> {
        val bilpleieregister = objectMapper.readValue(register, BilpleieregisterDTO::class.java)
        val registerParsed: MutableList<BilpleievirksomhetDTO> = mutableListOf()

        log.info("Parser hoved- og underenheter i ${bilpleieregister.registernavn}")
        bilpleieregister.data.forEach { hovedenhet ->
            registerParsed.add(hovedenhet)
            hovedenhet.underenheter?.let { underenheter ->
                registerParsed.addAll(underenheter.map { it })
            }
        }
        log.info("Parset ${registerParsed.size} enheter")
        return registerParsed
    }
}
