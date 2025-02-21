package no.nav.arbeid.registeroppslag.app.test

import no.nav.arbeid.registeroppslag.app.env
import no.nav.arbeid.registeroppslag.startApp

fun main() {
    val localAppCtx = TestApplicationContext(env)
    localAppCtx.startApp()
}
