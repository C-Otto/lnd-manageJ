plugins {
    `java-platform`
}

group = "de.c-otto.lndmanagej"

javaPlatform {
    allowDependencies()
}

dependencies {
    val springBootVersion = "3.2.2"
    val grpcVersion = "1.61.1"

    api(platform("org.springframework.cloud:spring-cloud-dependencies:2023.0.0"))
    api(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    api("io.projectreactor:reactor-core")

    constraints {
        api("com.google.ortools:ortools-java:9.8.3296")
        api("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
        api("io.grpc:grpc-netty:$grpcVersion")
        api("io.grpc:grpc-protobuf:$grpcVersion")
        api("io.grpc:grpc-stub:$grpcVersion")
        api("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")
        api("io.vavr:vavr:0.10.4")
    }
}
