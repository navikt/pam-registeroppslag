package no.nav.arbeid.registeroppslag.valkey

import io.valkey.JedisPool
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ValkeyService(private val pool: JedisPool) {
    fun set(key: String, value: String): String = pool.resource.use { it.set(key, value) }

    fun get(key: String): String? = pool.resource.use { it.get(key) }

    fun dbsize(): Long = pool.resource.use { it.dbSize() }

    fun flushdb(): String = pool.resource.use { it.flushDB() }
}