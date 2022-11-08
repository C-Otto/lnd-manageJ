plugins {
    id("lnd-manageJ.java-library-conventions")
}

dependencies {
    implementation(project(":model"))
    implementation(project(":caching"))
    implementation("org.ini4j:ini4j")
    testImplementation(testFixtures(project(":model")))
}
