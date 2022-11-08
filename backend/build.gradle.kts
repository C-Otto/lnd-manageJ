plugins {
    id("lnd-manageJ.java-library-conventions")
}

dependencies {
    implementation(project(":caching"))
    implementation(project(":configuration"))
    implementation(project(":forwarding-history"))
    implementation(project(":grpc-adapter"))
    implementation(project(":invoices"))
    implementation(project(":model"))
    implementation(project(":balances"))
    implementation(project(":onlinepeers"))
    implementation(project(":selfpayments"))
    implementation(project(":transactions"))
    testImplementation(testFixtures(project(":model")))
    testImplementation(testFixtures(project(":transactions")))
}

pitest {
    testStrengthThreshold.set(97)
}
