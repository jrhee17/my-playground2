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
        mavenBom("com.linecorp.armeria:armeria-bom:1.29.0")
        mavenBom("com.linecorp.centraldogma:centraldogma-bom:0.67.3")
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.0")
        mavenBom("io.netty:netty-bom:4.1.112.Final")
    }
}

dependencies {
    implementation("com.linecorp.armeria:armeria")
    implementation("io.netty:netty-all")
//    implementation("com.linecorp.armeria:armeria-spring-boot3-webflux-starter")
    implementation("io.projectreactor:reactor-core-micrometer:1.0.6")
//    runtimeOnly("com.linecorp.armeria:armeria-spring-boot3-actuator-starter")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("io.micrometer:micrometer-registry-prometheus")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.linecorp.armeria:armeria-junit5")

}

tasks.test {
    useJUnitPlatform()
}