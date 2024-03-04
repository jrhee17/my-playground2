plugins {
    id("java")
    kotlin("jvm") version "1.9.22"
}

group = "com.github.jrhee17"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Adjust the list as you need.
    listOf(
        "armeria",
        "armeria-graphql",
        "armeria-kotlin",
        "armeria-junit5",
        ).forEach {
        implementation("com.linecorp.armeria:${it}:1.27.2")
    }
    implementation("com.expediagroup:graphql-kotlin-schema-generator:7.0.2")

    // Logging
    runtimeOnly("ch.qos.logback:logback-classic:1.4.14")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.25.2")
    testImplementation("org.awaitility:awaitility:4.2.0")
    testImplementation("net.javacrumbs.json-unit:json-unit:2.38.0")
    testImplementation("net.javacrumbs.json-unit:json-unit-fluent:2.38.0")
}

tasks.test {
    useJUnitPlatform()
}