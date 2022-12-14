import com.google.protobuf.gradle.id

plugins {
    id("java-library")
    id("com.google.protobuf")
}

dependencies {
    api(platform("de.cotto.lndmanagej:platform"))
    protobuf(platform("de.cotto.lndmanagej:platform"))
    api("io.grpc:grpc-stub")
    api("io.grpc:grpc-protobuf")
    implementation("io.grpc:grpc-netty")
    implementation("commons-codec:commons-codec")
    implementation("javax.annotation:javax.annotation-api")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.21.11"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.51.1"
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

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    consistentResolution {
        useCompileClasspathVersions()
    }
}

repositories {
    mavenCentral()
}
