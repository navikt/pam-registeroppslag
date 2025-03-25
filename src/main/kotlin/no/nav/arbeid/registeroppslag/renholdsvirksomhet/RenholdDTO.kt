package no.nav.arbeid.registeroppslag.renholdsvirksomhet

import com.fasterxml.jackson.databind.JsonNode
import no.nav.arbeid.registeroppslag.Organisasjonsnummer
import no.nav.arbeid.registeroppslag.Registerstatus
import no.nav.arbeid.registeroppslag.RegisterstatusDTO

data class RenholdsvirksomhetDTO(
    val organisasjonsnummer: Organisasjonsnummer,
    val navn: String,
    val godkjenningsstatus: String,
) {
    companion object {
        val registernavn = "renholdsregisteret"

        /**
         * Oppretter en [RenholdsvirksomhetDTO] fra en [JsonNode]
         *
         * Underenheter fra registerdataen til Arbeidstilsynet inneholder ikke en egen godkjenningsstatus,
         * men skal ha samme godkjenningsstatus som hovedenheten. Denne kan sendes inn et parameter.
         */
        fun fraJson(json: JsonNode, godkjenningsstatus: String? = null): RenholdsvirksomhetDTO {
            return RenholdsvirksomhetDTO(
                organisasjonsnummer = Organisasjonsnummer(json["Organisasjonsnummer"].asText()),
                navn = json["Navn"].asText(),
                godkjenningsstatus = godkjenningsstatus ?: json["Status"].asText(),
            )
        }

        fun ikkeRegistrert(): RenholdsvirksomhetDTO {
            return RenholdsvirksomhetDTO(
                organisasjonsnummer = Organisasjonsnummer("000000000"),
                navn = "",
                godkjenningsstatus = "Ikke Registrert",
            )
        }
    }

    fun tilRegisterstatus(): RegisterstatusDTO {
        return RegisterstatusDTO(
            registernavn = registernavn,
            statusTekst = godkjenningsstatus,
            status = registerstatusTransformator(godkjenningsstatus)
        )
    }

    private infix fun String.matcher(regex: String) = this.matches(Regex(regex, RegexOption.IGNORE_CASE))
    private fun registerstatusTransformator(status: String): Registerstatus {
        return when {
            status matcher "ikke registrert" -> Registerstatus.IKKE_REGISTRERT
            status matcher "ikke godkjent" -> Registerstatus.IKKE_GODKJENT
            status matcher "under (behandling|hms-kortbestilling)" -> Registerstatus.GODKJENT
            status matcher "godkjent (med ansatte|uten ansatte)" -> Registerstatus.GODKJENT
            else -> throw IllegalArgumentException("Ukjent godkjenningsstatus: $status, for virksomhet: $this")
        }
    }
}