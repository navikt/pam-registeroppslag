package no.nav.arbeid.registeroppslag.app.test

import no.nav.arbeid.registeroppslag.app.env
import no.nav.arbeid.registeroppslag.startApp

abstract class TestRunningApplication {

    companion object {
        const val lokalUrlBase = "http://localhost:8080"

        @JvmStatic
        val appCtx = TestApplicationContext(env)
        val javalin = appCtx.startApp()
    }

}
