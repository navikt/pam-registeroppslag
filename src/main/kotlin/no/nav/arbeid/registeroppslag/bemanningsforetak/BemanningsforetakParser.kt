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
     * Parser en [ByteArray] med data fra bemanningsforetaksregisteret.
     *
     * Dataen er i JSON-format og må ha følgende format:
     * ```
     * {
     *   metadata: {
     *     versjon: "X.Y",
     *     datoTidGenerert: "2025-01-01T00:00:00.0000000Z",
     *   },
     *   registernavn: "bemanningsforetaksregisteret",
     *   data: [
     *     {
     *       organisasjonsnummer: "000000000",
     *       overordnetEnhet: null,
     *       navn: "BEDRIFT AS",
     *       registerstatusTekst: "Godkjent",
     *       registerstatus: 1,
     *       godkjenningsstatus: "Godkjent",
     *       underenheter: [
     *         {
     *           organisasjonsnummer: "111111111",
     *           overordnetEnhet: "000000000",
     *           navn: "UNDERENHET AS",
     *           registerstatusTekst: "Godkjent",
     *           registerstatus: 1,
     *           godkjenningsstatus: "Godkjent",
     *           underenheter: []
     *         }
     *       ]
     *     }
     *   ]
     * }
     * ```
     *
     * Funksjonen vil parse alle enheter og underenheter i filen og samle dem i en liste.
     *
     * @param register En byte-array med data fra bemanningsforetaksregisteret
     *
     * @return En liste med DTO-er for bemanningsforetak
     */
    override fun parseRegister(register: ByteArray): List<BemanningsforetakDTO> {
        val registerData = objectMapper.readTree(register)["data"]
        val registerParsed: MutableList<BemanningsforetakDTO> = mutableListOf()

        log.info("Legger til enheter i bemanningsforetaksregisteret")
        registerData.forEach { enhet ->
            registerParsed.add(BemanningsforetakDTO.fraJson(enhet)) // Legger til hovedenhet
            enhet["underenheter"]?.let { underenheter ->
                registerParsed.addAll(underenheter.map { BemanningsforetakDTO.fraJson(it) }) // Legger til underenheter
            }
        }

        return registerParsed
    }
}