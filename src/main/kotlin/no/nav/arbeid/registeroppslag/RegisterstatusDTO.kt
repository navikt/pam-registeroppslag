package no.nav.arbeid.registeroppslag

data class RegisterstatusDTO(
    val registernavn: String,
    val statusTekst: String,
    val status: Registerstatus,
)

enum class Registerstatus(val kode: Int) {
    IKKE_REGISTRERT(0),
    GODKJENT(1),
    IKKE_GODKJENT(2)
}