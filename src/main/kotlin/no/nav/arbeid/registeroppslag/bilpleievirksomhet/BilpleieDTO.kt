package no.nav.arbeid.registeroppslag.bilpleievirksomhet

import no.nav.arbeid.registeroppslag.Organisasjonsnummer
import no.nav.arbeid.registeroppslag.Registerstatus
import no.nav.arbeid.registeroppslag.RegisterstatusDTO

data class BilpleievirksomhetDTO(
    val organisasjonsnummer: Organisasjonsnummer,
    val navn: String,
    val registerstatus: Bilpleiestatus,
    val registerstatusTekst: String,
    val godkjenningsstatus: String,
    val underenheter: List<BilpleievirksomhetDTO>?,
) {
    companion object {
        val registernavn = "bilpleieregisteret"

        fun ikkeRegistrert(): BilpleievirksomhetDTO {
            return BilpleievirksomhetDTO(
                organisasjonsnummer = Organisasjonsnummer("000000000"),
                navn = "",
                registerstatus = Bilpleiestatus.UKJENT,
                registerstatusTekst = "Ikke registrert",
                godkjenningsstatus = "Ikke registrert",
                underenheter = emptyList(),
            )
        }
    }

    fun tilRegisterstatus(): RegisterstatusDTO {
        return RegisterstatusDTO(
            registernavn = registernavn,
            statusTekst = registerstatusTekst,
            status = registerstatusMapper(registerstatus),
        )
    }
}

private fun registerstatusMapper(status: Bilpleiestatus): Registerstatus {
    return when (status) {
        Bilpleiestatus.UKJENT -> Registerstatus.IKKE_REGISTRERT
        Bilpleiestatus.SØKNAD_UNDER_BEHANDLING -> Registerstatus.GODKJENT
        Bilpleiestatus.HMS_KORTBESTILLING_UNDER_BEHANDLING -> Registerstatus.GODKJENT
        Bilpleiestatus.GODKJENT_UTEN_ANSATTE -> Registerstatus.GODKJENT
        Bilpleiestatus.GODKJENT_MED_ANSATTE -> Registerstatus.GODKJENT
        Bilpleiestatus.IKKE_GODKJENT -> Registerstatus.IKKE_GODKJENT
        Bilpleiestatus.GODKJENT_AV_STATENS_VEGVESEN -> Registerstatus.GODKJENT
    }
}

enum class Bilpleiestatus (val status: Int) {
    UKJENT(0),
    SØKNAD_UNDER_BEHANDLING(1),
    HMS_KORTBESTILLING_UNDER_BEHANDLING(2),
    GODKJENT_UTEN_ANSATTE(3),
    GODKJENT_MED_ANSATTE(4),
    IKKE_GODKJENT(5),
    GODKJENT_AV_STATENS_VEGVESEN(6),
}

data class Metadata(val versjon: String, val datoTidGenerert: String)
data class BilpleieregisterDTO(
    val metadata: Metadata,
    val registernavn: String,
    val data: List<BilpleievirksomhetDTO>,
)
