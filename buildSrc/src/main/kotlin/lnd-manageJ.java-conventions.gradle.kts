plugins {
    id("de.c-otto.java-conventions")
    id("lnd-manageJ.tests")
    id("lnd-manageJ.integration-tests")
    id("org.springframework.boot")
    id("java-test-fixtures")
}


dependencies {
    implementation(platform("de.c-otto.lndmanagej:platform"))
    testFixturesImplementation(platform("de.c-otto.lndmanagej:platform"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.apache.commons:commons-lang3")
    implementation("com.google.code.findbugs:jsr305")
    implementation("com.ryantenney.metrics:metrics-spring")
    implementation("com.google.guava:guava")
}
