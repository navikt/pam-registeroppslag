package no.nav.arbeid.registeroppslag.bemanningsforetak

import com.fasterxml.jackson.databind.JsonNode
import no.nav.arbeid.registeroppslag.Organisasjonsnummer
import no.nav.arbeid.registeroppslag.Registerstatus

data class BemanningsforetakDTO(
    val registernavn: String = "bemanningsforetaksregisteret",
    val organisasjonsnummer: Organisasjonsnummer,
    val overordnetEnhet: String?,
    val navn: String,
    val registerstatus: Registerstatus,
    val registerstatusTekst: String,
    val godkjenningsstatus: String,
) {
    companion object {
        fun fraJson(jsonNode: JsonNode): BemanningsforetakDTO {
            if (jsonNode.isEmpty) return ikkeRegistrert()

            return BemanningsforetakDTO(
                organisasjonsnummer = Organisasjonsnummer(jsonNode["organisasjonsnummer"].asText()),
                overordnetEnhet = jsonNode["overordnetEnhet"]?.asText(),
                navn = jsonNode["navn"].asText(),
                registerstatus = Registerstatus.entries[jsonNode["registerstatus"].asInt()],
                registerstatusTekst = jsonNode["registerstatusTekst"].asText(),
                godkjenningsstatus = jsonNode["godkjenningsstatus"].asText(),
            )
        }

        fun ikkeRegistrert(): BemanningsforetakDTO {
            return BemanningsforetakDTO(
                organisasjonsnummer = Organisasjonsnummer("000000000"),
                overordnetEnhet = null,
                navn = "",
                registerstatus = Registerstatus.IKKE_REGISTRERT,
                registerstatusTekst = "Ikke registrert",
                godkjenningsstatus = "Ikke Registrert",
            )
        }
    }
}