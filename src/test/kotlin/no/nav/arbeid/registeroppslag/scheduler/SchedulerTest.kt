package no.nav.arbeid.registeroppslag.scheduler

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class SchedulerTest {
    var testListe = mutableListOf<Int>()
    val scheduler = Scheduler("/2 * * * * ?") { testListe.add(1) }

    @AfterEach
    fun ryddOpp() {
        testListe.clear()
        scheduler.stop()
    }

    @Test
    fun `Skal starte scheduleren`() {
        scheduler.start()
        Thread.sleep(5000)
        assertThat(testListe).isNotEmpty()
    }
}