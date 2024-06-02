plugins {
    id("lnd-manageJ.java-library-conventions")
}

dependencies {
    implementation(project(":backend"))
    implementation(project(":balances"))
    implementation(project(":fee-rates"))
    implementation(project(":caching"))
    implementation(project(":model"))
    implementation(project(":onlinepeers"))
    implementation(project(":privatechannels"))
    testFixturesApi(testFixtures(project(":model")))
}
