import com.github.spotbugs.snom.SpotBugsTask
import com.google.protobuf.gradle.id
import de.aaschmid.gradle.plugins.cpd.Cpd
import de.cotto.javaconventions.plugins.JacocoPlugin.CheckForExecutionDataTask
import info.solidsoft.gradle.pitest.PitestTask

plugins {
    id("lnd-manageJ.java-library-conventions")
    id("com.google.protobuf")
}

dependencies {
    api("io.grpc:grpc-stub")
    api("io.grpc:grpc-protobuf")
    implementation("io.grpc:grpc-netty")
    implementation("commons-codec:commons-codec")
    implementation("javax.annotation:javax.annotation-api")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.23.4"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.56.1"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
            }
        }
    }
}
sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/grpc")
            srcDirs("build/generated/source/proto/main/java")
        }
    }
}

tasks.withType<Checkstyle>().configureEach {
    enabled = false
}
tasks.withType<Cpd>().configureEach {
    enabled = false
}
tasks.withType<Pmd>().configureEach {
    enabled = false
}
tasks.withType<SpotBugsTask>().configureEach {
    enabled = false
}
tasks.withType<CheckForExecutionDataTask>().configureEach {
    enabled = false
}
tasks.withType<PitestTask>().configureEach {
    enabled = false
}
