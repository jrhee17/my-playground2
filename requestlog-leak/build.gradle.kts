import com.google.protobuf.gradle.*

plugins {
    id("java")
    id("io.spring.dependency-management") version "1.1.4"
    id("com.google.protobuf") version "0.9.4"
}

group = "com.github.jrhee17"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("com.linecorp.armeria:armeria-bom:1.30.1")
    }
}

dependencies {
    implementation("com.linecorp.armeria:armeria")
    implementation("com.linecorp.armeria:armeria-junit5")
    implementation("com.linecorp.armeria:armeria-grpc")
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
    testImplementation("org.awaitility:awaitility:4.2.2")
    testImplementation("org.assertj:assertj-core:3.26.3")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.11.3")
// https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation("ch.qos.logback:logback-classic:1.5.12")
}

tasks.withType<Test> {
//    maxHeapSize = "4096m"
}

tasks.test {
    useJUnitPlatform()
}

protobuf {
    protoc {
        // The artifact spec for the Protobuf Compiler
        artifact = "com.google.protobuf:protoc:3.25.1"
    }

    // Locate the codegen plugins.
    plugins {
        // Locate a plugin with name 'grpc'.
        id("grpc") {
            // Download from the repository.
            artifact = "io.grpc:protoc-gen-grpc-java:1.64.0"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                // Apply the "grpc" plugin whose spec is defined above, without
                // options. Note the braces cannot be omitted, otherwise the
                // plugin will not be added. This is because of the implicit way
                // NamedDomainObjectContainer binds the methods.
                id("grpc") { }
            }
        }
    }
}
