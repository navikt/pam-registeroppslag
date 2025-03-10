package no.nav.arbeid.registeroppslag.metrikker

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry

class Metrikker(meterRegistry: PrometheusMeterRegistry) {
    private val bemanningsforetakNedlastetCounter = meterRegistry.counter("pam_registeroppslag_nedlasting_bemanningsforetak")

    fun lastetNedOgLagretBemmaningsforetak() = bemanningsforetakNedlastetCounter.increment()
}