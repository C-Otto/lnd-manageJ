plugins {
    id("lnd-manageJ.java-library-conventions")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation(project(":backend"))
    implementation(project(":pickhardt-payments"))
    implementation(project(":model"))
    testImplementation(testFixtures(project(":model")))
    testImplementation(testFixtures(project(":pickhardt-payments")))
    integrationTestImplementation("com.ryantenney.metrics:metrics-spring")
    integrationTestImplementation("org.springframework.boot:spring-boot-starter-webflux")
    integrationTestImplementation(project(":backend"))
    integrationTestImplementation(project(":grpc-adapter"))
    integrationTestImplementation(testFixtures(project(":model")))
    integrationTestImplementation(testFixtures(project(":pickhardt-payments")))
}

pitest {
    testStrengthThreshold.set(99)
}
