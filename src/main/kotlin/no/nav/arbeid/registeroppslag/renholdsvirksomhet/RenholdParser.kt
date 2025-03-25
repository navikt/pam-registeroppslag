package no.nav.arbeid.registeroppslag.renholdsvirksomhet

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.arbeid.registeroppslag.RegisterParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class RenholdParser(
    private val objectMapper: ObjectMapper
) : RegisterParser {
    companion object {
        val log: Logger = LoggerFactory.getLogger(RenholdParser::class.java)
    }

    /**
     * Parser renholdsregisteret og returnerer en liste med hoved- og underenheter fra registeret
     * slik at man kan slå opp på både hoved- og underenheter.
     *
     * XML-filen inneholder en liste med hovedenheter hvor hver hovedenhet kan ha en liste med underenheter,
     * eller en enkelt underenhet.
     * Det er kun hovedenhetene som har definert godkjenningsstatus, men underenhetene skal ha samme godkjenningsstatus.
     * I parseren kopieres godkjenningsstatus fra hovedenhet til underenhet.
     *
     * @param register [ByteArray] med registerdata
     * @return [List]<[RenholdsvirksomhetDTO]> med hoved- og underenheter i registeret
     */
    override fun parseRegister(register: ByteArray): List<RenholdsvirksomhetDTO> {
        val registerdata = objectMapper.readTree(register) as ObjectNode
        log.info("Parser hoved- og underenheter i renholdsregisteret")

        val registerParsed = registerdata.first().flatMap { hovedenhet ->
            val hovedenhetDTO = RenholdsvirksomhetDTO.fraJson(hovedenhet)
            val underenheter = hovedenhet["Underavdelinger"].flatMap { underenhet ->
                when (underenhet.isArray) {
                    true -> underenhet.map { RenholdsvirksomhetDTO.fraJson(it, hovedenhetDTO.godkjenningsstatus) }
                    false -> listOf(RenholdsvirksomhetDTO.fraJson(underenhet, hovedenhetDTO.godkjenningsstatus))
                }
            }
            return@flatMap listOf(hovedenhetDTO) + underenheter
        }

        log.info("Parset ${registerParsed.size} enheter")
        return registerParsed
    }
}