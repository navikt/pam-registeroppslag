package no.nav.arbeid.registeroppslag.bemanningsforetak

import no.nav.arbeid.registeroppslag.Organisasjonsnummer
import no.nav.arbeid.registeroppslag.Registerstatus
import no.nav.arbeid.registeroppslag.RegisterstatusDTO

data class BemanningsforetakDTO(
    val organisasjonsnummer: Organisasjonsnummer,
    val overordnetEnhet: String?,
    val navn: String,
    val registerstatus: Registerstatus,
    val registerstatusTekst: String,
    val godkjenningsstatus: String,
    val underenheter: List<BemanningsforetakDTO>?,
) {
    companion object {
        val registernavn = "bemanningsforetaksregisteret"

        fun ikkeRegistrert(): BemanningsforetakDTO {
            return BemanningsforetakDTO(
                organisasjonsnummer = Organisasjonsnummer("000000000"),
                overordnetEnhet = null,
                navn = "",
                registerstatus = Registerstatus.IKKE_REGISTRERT,
                registerstatusTekst = "Ikke registrert",
                godkjenningsstatus = "Ikke Registrert",
                underenheter = emptyList(),
            )
        }
    }

    fun tilRegisterstatus(): RegisterstatusDTO {
        return RegisterstatusDTO(
            registernavn = registernavn,
            statusTekst = godkjenningsstatus,
            status = registerstatus,
        )
    }
}

data class Metadata(val versjon: String, val datoTidGenerert: String)
data class BemanningsforetaksregisterDTO(
    val metadata: Metadata,
    val registernavn: String,
    val data: List<BemanningsforetakDTO>,
)