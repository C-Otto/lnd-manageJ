plugins {
    id("lnd-manageJ.java-library-conventions")
}

dependencies {
    implementation(project(":ui"))
    implementation(project(":model"))
    implementation(project(":web"))
    implementation(testFixtures(project(":model")))
    integrationTestImplementation(testFixtures(project(":ui")))
}

pitest {
    testStrengthThreshold.set(0)
}
tasks.jacocoTestCoverageVerification {
    violationRules {
        rules.forEach {rule ->
            rule.limits.forEach {limit ->
                if (limit.counter == "INSTRUCTION") {
                    limit.minimum = 0.toBigDecimal()
                }
                if (limit.counter == "CLASS") {
                    limit.minimum = 0.toBigDecimal()
                }
                if (limit.counter == "BRANCH") {
                    limit.minimum = 0.toBigDecimal()
                }
                if (limit.counter == "METHOD") {
                    limit.minimum = 0.toBigDecimal()
                }
            }
        }
    }
}
