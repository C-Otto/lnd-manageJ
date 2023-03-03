plugins {
    `java-platform`
}

group = "de.c-otto.lndmanagej"

javaPlatform {
    allowDependencies()
}

dependencies {
    val springBootVersion = "3.0.4"
    val grpcVersion = "1.51.1"

    api(platform("org.springframework.cloud:spring-cloud-dependencies:2022.0.0"))
    api(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))

    constraints {
        api("com.google.ortools:ortools-java:9.5.2237")
        api("com.google.protobuf:protobuf-gradle-plugin:0.9.2")
        api("io.grpc:grpc-netty:$grpcVersion")
        api("io.grpc:grpc-protobuf:$grpcVersion")
        api("io.grpc:grpc-stub:$grpcVersion")
        api("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")
        api("io.vavr:vavr:0.10.4")
    }
}
