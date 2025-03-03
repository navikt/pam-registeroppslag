package no.nav.arbeid.registeroppslag.scheduler

import org.quartz.*
import org.quartz.JobBuilder.newJob
import org.quartz.TriggerBuilder.newTrigger
import org.quartz.impl.StdSchedulerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Scheduler(
    private val tidspunkt: String = "0 0 0 * * ? *", // setter default til 00:00 hver dag
    private val job: () -> Unit,
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(Scheduler::class.java)
    }

    private val scheduler = StdSchedulerFactory.getDefaultScheduler()

    private val trigger = newTrigger()
        .withSchedule(CronScheduleBuilder.cronSchedule(tidspunkt))
        .withIdentity("nedlasterTrigger", "nedlaster")
        .build()

    private val jobDetail = newJob().ofType(NedlasterJob::class.java)
        .withIdentity("nedlasterJob", "nedlaster")
        .build()
        .apply { jobDataMap["job"] = job }

    fun start() {
        log.info("Starter scheduler for nedlasting med intervall $tidspunkt")
        scheduler.start()
        scheduler.scheduleJob(jobDetail, trigger)
        log.info("Trigger jobb ved oppstart")
        scheduler.triggerJob(jobDetail.key)
    }

    fun stop() {
        log.info("Stopper scheduler")
        scheduler.shutdown()
    }


}