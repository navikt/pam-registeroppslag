package no.nav.arbeid.registeroppslag.valkey

import io.valkey.DefaultJedisClientConfig
import io.valkey.HostAndPort
import io.valkey.JedisPool
import io.valkey.JedisPoolConfig

fun opprettPool(env: Map<String, String>): JedisPool {
    return JedisPool(
        JedisPoolConfig(),
        HostAndPort(
            env.getValue("VALKEY_HOST_REGISTEROPPSLAG"),
            env.getValue("VALKEY_PORT_REGISTEROPPSLAG").toInt()
        ),
        DefaultJedisClientConfig.builder()
            .user(env.getValue("VALKEY_USERNAME_REGISTEROPPSLAG"))
            .password(env.getValue("VALKEY_PASSWORD_REGISTEROPPSLAG"))
            .ssl(env.getValue("VALKEY_USE_TLS").toBoolean())
            .build()
        )
}
