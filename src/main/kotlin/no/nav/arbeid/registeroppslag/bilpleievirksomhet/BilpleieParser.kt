package no.nav.arbeid.registeroppslag.bilpleievirksomhet

import com.fasterxml.jackson.databind.ObjectMapper
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderHeaderAwareBuilder
import no.nav.arbeid.registeroppslag.Organisasjonsnummer
import no.nav.arbeid.registeroppslag.RegisterParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.StringReader

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

    fun parseBilverksted(register: ByteArray): List<BilpleievirksomhetDTO> {
        val bilverksted = mutableListOf<BilpleievirksomhetDTO>()
        val csvReader = CSVReaderHeaderAwareBuilder(StringReader(register.decodeToString()))
            .withCSVParser(CSVParserBuilder().withSeparator(';').build())
            .build()
        val rader = csvReader.readAll()
        val BEDRIFTSNAVN = 0
        val ORGANISASJONSNUMMER = 5
        log.info("Parser bilverksted fra CSV med ${rader.size} rader")
        rader.forEach { rad ->
            val navn = rad[BEDRIFTSNAVN]?.trim() ?: ""
            val orgnr = rad[ORGANISASJONSNUMMER]?.trim() ?: ""
            if (navn.isNotEmpty() && orgnr.isNotEmpty()) {
                bilverksted.add(
                    BilpleievirksomhetDTO(
                        organisasjonsnummer = Organisasjonsnummer(orgnr),
                        navn = navn,
                        registerstatus = Bilpleiestatus.GODKJENT_AV_STATENS_VEGVESEN,
                        registerstatusTekst = "Godkjent av Statens Vegvesen",
                        godkjenningsstatus = "Godkjent av Statens Vegvesen",
                        underenheter = emptyList(),
                    )
                )
            } else {
                log.warn("Rad i bilverksted CSV mangler navn eller organisasjonsnummer: $rad")
            }
        }
        return bilverksted
    }
}
