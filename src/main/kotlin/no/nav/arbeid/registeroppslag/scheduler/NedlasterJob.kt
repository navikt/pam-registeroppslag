package no.nav.arbeid.registeroppslag.scheduler

import org.quartz.Job
import org.quartz.JobExecutionContext

class NedlasterJob: Job {
    lateinit var job: () -> Unit

    override fun execute(context: JobExecutionContext) {
        job()
    }
}