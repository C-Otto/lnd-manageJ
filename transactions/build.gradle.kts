plugins {
    id("lnd-manageJ.java-library-conventions")
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("io.github.resilience4j:resilience4j-spring-boot3")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation(project(":model"))
    implementation(project(":caching"))
    implementation(project(":grpc-adapter"))
    runtimeOnly("io.vavr:vavr")
    testFixturesApi(testFixtures(project(":model")))
}

pitest {
    testStrengthThreshold.set(97)
}
