plugins {
    id("lnd-manageJ.java-library-conventions")
}

dependencies {
    implementation(project(":caching"))
    implementation(project(":grpc-client"))
    implementation(project(":configuration"))
    implementation(project(":model"))
    testImplementation("org.awaitility:awaitility")
    testImplementation(testFixtures(project(":model")))
}

pitest {
    testStrengthThreshold.set(99)
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rules.forEach {rule ->
            rule.limits.forEach {limit ->
                if (limit.counter == "INSTRUCTION") {
                    limit.minimum = 0.89.toBigDecimal()
                }
                if (limit.counter == "METHOD") {
                    limit.minimum = 0.80.toBigDecimal()
                }
                if (limit.counter == "BRANCH") {
                    limit.minimum = 0.95.toBigDecimal()
                }
            }
        }
    }
}
