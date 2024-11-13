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

tasks.test {
    useJUnitPlatform()
}