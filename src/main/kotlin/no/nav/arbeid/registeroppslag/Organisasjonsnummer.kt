package no.nav.arbeid.registeroppslag

@JvmInline
value class Organisasjonsnummer(private val orgnr: String) {
    init {
        require(orgnr.matches(Regex("\\d{9}"))) { "Ugyldig organisasjonsnummer" }
    }

    override fun toString() = orgnr
}