plugins {
    `java-platform`
}

group = "de.cotto.lndmanagej"

javaPlatform {
    allowDependencies()
}

dependencies {
    val springBootVersion = "3.0.1"
    val grpcVersion = "1.51.1"
    api(platform("org.springframework.cloud:spring-cloud-dependencies:2022.0.0-RC3"))
    api(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))

    constraints {
        api("com.adarshr:gradle-test-logger-plugin:3.2.0")
        api("com.github.ben-manes.caffeine:caffeine:3.1.2")
        api("com.github.spotbugs.snom:spotbugs-gradle-plugin:5.0.13")
        api("com.google.code.findbugs:jsr305:3.0.2")
        api("com.google.errorprone:error_prone_core:2.16")
        api("com.google.guava:guava:31.1-jre")
        api("com.google.ortools:ortools-java:9.5.2237")
        api("com.google.protobuf:protobuf-gradle-plugin:0.9.1")
        api("com.ryantenney.metrics:metrics-spring:3.1.3")
        api("com.tngtech.archunit:archunit:1.0.1")
        api("com.uber.nullaway:nullaway:0.10.5")
        api("commons-codec:commons-codec:1.15")
        api("de.aaschmid:gradle-cpd-plugin:3.3")
        api("info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.9.11")
        api("io.github.resilience4j:resilience4j-spring-boot2:2.0.2")
        api("io.grpc:grpc-netty:$grpcVersion")
        api("io.grpc:grpc-protobuf:$grpcVersion")
        api("io.grpc:grpc-stub:$grpcVersion")
        api("javax.annotation:javax.annotation-api:1.3.2")
        api("net.ltgt.gradle:gradle-errorprone-plugin:3.0.1")
        api("net.ltgt.gradle:gradle-nullaway-plugin:1.5.0")
        api("nl.jqno.equalsverifier:equalsverifier:3.12.2")
        api("org.apache.commons:commons-lang3:3.12.0")
        api("org.awaitility:awaitility:4.2.0")
        api("org.eclipse.collections:eclipse-collections:11.1.0")
        api("org.ini4j:ini4j:0.5.4")
        api("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")
        api("org.webjars:bootstrap:5.2.3")
        api("org.webjars:webjars-locator:0.46")
        api("com.github.valfirst:slf4j-test:2.6.1")
        api("io.vavr:vavr:0.10.4")
    }
}
