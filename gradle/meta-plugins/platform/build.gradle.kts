plugins {
    `java-platform`
}

group = "de.c-otto.lndmanagej"

javaPlatform {
    allowDependencies()
}

dependencies {
    val springBootVersion = "4.0.6"
    val grpcVersion = "1.81.0"

    api(platform("org.springframework.cloud:spring-cloud-dependencies:2025.1.1"))
    api(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    api("io.projectreactor:reactor-core")

    constraints {
        api("com.google.ortools:ortools-java:9.15.6755")
        api("com.google.protobuf:protobuf-gradle-plugin:0.10.0")
        api("io.grpc:grpc-netty:$grpcVersion")
        api("io.grpc:grpc-protobuf:$grpcVersion")
        api("io.grpc:grpc-stub:$grpcVersion")
        api("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")
        api("io.vavr:vavr:1.0.1")
        api("net.javacrumbs.json-unit:json-unit-assertj:5.1.1")
    }
}
