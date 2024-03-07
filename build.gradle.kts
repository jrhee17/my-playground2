plugins {
    id("java")
    kotlin("jvm") version "1.9.22"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.github.jrhee17"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("com.linecorp.armeria:armeria-bom:1.27.0")
        mavenBom("com.linecorp.centraldogma:centraldogma-bom:0.61.1")
    }
}

dependencies {
    // Adjust the list as you need.
    listOf(
        "armeria",
        "armeria-graphql",
        "armeria-kotlin",
        "armeria-junit5",
        ).forEach {
        implementation("com.linecorp.armeria:${it}")
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