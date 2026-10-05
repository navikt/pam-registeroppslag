import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.4.20"
    id("com.gradleup.shadow") version "9.6.1"
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
    application
}

application {
    mainClass.set("no.nav.arbeid.registeroppslag.ApplicationKt")
}

repositories {
    mavenCentral()
    maven { url = uri("https://packages.confluent.io/maven/") }
    maven("https://jitpack.io")
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<ShadowJar> {
    archiveFileName.set("pam-registeroppslag-all.jar")
    mergeServiceFiles()
}

val jacksonVersion = "2.22.3"
val javalinVersion = "7.2.3"
val micrometerVersion = "1.17.1"
val tokenSupportVersion = "5.0.30"
val testContainersVersion = "2.0.5"
dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("io.javalin:javalin:$javalinVersion")
    implementation("io.javalin:javalin-micrometer:$javalinVersion")
    implementation("org.eclipse.jetty:jetty-util")
    implementation("io.micrometer:micrometer-core:$micrometerVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")
    implementation("io.prometheus:simpleclient_common:0.16.0")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jacksonVersion")

    implementation("com.opencsv:opencsv:5.12.0")

    implementation("no.nav.security:token-validation-core:$tokenSupportVersion")
    implementation("no.nav.security:token-client-core:$tokenSupportVersion")

    implementation("ch.qos.logback:logback-classic:1.6.4")
    implementation("net.logstash.logback:logstash-logback-encoder:9.0")
    implementation("com.papertrailapp:logback-syslog4j:1.0.0")
    implementation("org.codehaus.janino:janino:3.1.12")
    implementation("com.auth0:java-jwt:4.6.1")

    implementation("io.valkey:valkey-java:5.5.0")
    implementation("org.quartz-scheduler:quartz:2.5.2")

    testImplementation(kotlin("test"))
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("io.mockk:mockk:1.14.11")
    testImplementation("no.nav.security:mock-oauth2-server:6.0.3")

    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
}
