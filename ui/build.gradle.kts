plugins {
    id("lnd-manageJ.java-library-conventions")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.webjars:webjars-locator")
    implementation("org.webjars:bootstrap")
    implementation(project(":backend"))
    implementation(project(":model"))
    implementation(project(":web"))
    testImplementation(testFixtures(project(":model")))
    integrationTestImplementation(project(":web"))
    integrationTestImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    integrationTestImplementation(testFixtures(project(":model")))
    integrationTestImplementation(testFixtures(project(":ui")))
    testFixturesImplementation(project(":model"))
    testFixturesImplementation(testFixtures(project(":model")))
    testFixturesImplementation(project(":web"))
}
