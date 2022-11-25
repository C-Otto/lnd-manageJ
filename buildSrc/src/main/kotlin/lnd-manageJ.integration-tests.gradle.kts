plugins {
    id("lnd-manageJ.tests")
}

testing {
    suites {
        this.register("integrationTest", JvmTestSuite::class) {
            testType.set(TestSuiteType.INTEGRATION_TEST)
            dependencies {
                implementation(project())
                implementation(project.dependencies.platform("de.cotto.lndmanagej:platform"))
                implementation("com.tngtech.archunit:archunit")
                implementation("org.awaitility:awaitility")
            }

            targets {
                configureEach {
                    testTask.configure {
                        shouldRunAfter(testing.suites.named("test"))
                    }
                }
            }
        }
    }
}

tasks.named("check") {
    dependsOn(testing.suites.named("integrationTest"))
}

testing.suites.named("integrationTest") {
    testlogger {
        slowThreshold = 2000
    }
}
