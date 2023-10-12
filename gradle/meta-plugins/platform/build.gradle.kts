plugins {
    `java-platform`
}

group = "de.c-otto.lndmanagej"

javaPlatform {
    allowDependencies()
}

dependencies {
    val springBootVersion = "3.1.4"
    val grpcVersion = "1.58.0"

    api(platform("org.springframework.cloud:spring-cloud-dependencies:2022.0.4"))
    api(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    api("io.projectreactor:reactor-core")

    constraints {
        api("com.google.ortools:ortools-java:9.6.2534")
        api("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
        api("io.grpc:grpc-netty:$grpcVersion")
        api("io.grpc:grpc-protobuf:$grpcVersion")
        api("io.grpc:grpc-stub:$grpcVersion")
        api("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")
        api("io.vavr:vavr:0.10.4")
    }
}
