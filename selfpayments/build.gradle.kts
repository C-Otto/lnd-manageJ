plugins {
    id("lnd-manageJ.java-library-conventions")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation(project(":model"))
    implementation(project(":invoices"))
    implementation(project(":payments"))
    testFixturesApi(testFixtures(project(":model")))
    integrationTestRuntimeOnly("com.h2database:h2")
    integrationTestImplementation(project(":invoices"))
    integrationTestImplementation(project(":payments"))
    integrationTestImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    integrationTestImplementation(testFixtures(project(":model")))
}
