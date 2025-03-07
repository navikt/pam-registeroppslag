package no.nav.arbeid.registeroppslag.valkey

import io.valkey.JedisPool
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ValkeyService(private val pool: JedisPool) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(ValkeyService::class.java)
    }

    fun set(key: String, value: String): String = pool.resource.use { it.set(key, value) }

    fun get(key: String): String? = pool.resource.use { it.get(key) }

    fun dbsize(): Long = pool.resource.use { it.dbSize() }

    fun flushdb(): String {
        log.info("Flusher ${dbsize()} keys i database")
        val res = pool.resource.use {
        log.info("Bruker DB ${it.db}")
            it.flushDB() }
        log.info("Flushet database, returnerte $res")
        return res
    }

    fun getallKeys(): Set<String> = pool.resource.use { it.keys("*") }
}