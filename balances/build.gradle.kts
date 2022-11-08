plugins {
    id("lnd-manageJ.java-library-conventions")
}

dependencies {
    implementation(project(":model"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testFixturesApi(testFixtures(project(":model")))
    integrationTestImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    integrationTestRuntimeOnly("com.h2database:h2")
    integrationTestImplementation(testFixtures(project(":balances")))
}
