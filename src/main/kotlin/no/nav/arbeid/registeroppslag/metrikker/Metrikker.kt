package no.nav.arbeid.registeroppslag.metrikker

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry

class Metrikker(meterRegistry: PrometheusMeterRegistry) {
    private val bemanningsforetakNedlastetCounter = meterRegistry.counter("pam_registeroppslag_nedlasting_bemanningsforetak")
    private val renholdsregisterNedlastetCounter = meterRegistry.counter("pam_registeroppslag_nedlasting_renholdsregister")
    private val bilpleieregisterNedlastetCounter = meterRegistry.counter("pam_registeroppslag_nedlasting_bilpleieregister")

    fun lastetNedOgLagretBemmaningsforetak() = bemanningsforetakNedlastetCounter.increment()
    fun lastetNedOgLagretRenholdsregister() = renholdsregisterNedlastetCounter.increment()
    fun lastetNedOgLagretBilpleieregister() = bilpleieregisterNedlastetCounter.increment()
}
