plugins {
    id("jacoco")
}

tasks.named("check") {
    dependsOn(tasks.withType<JacocoCoverageVerification>())
}

tasks.withType<JacocoReport>().configureEach {
    mustRunAfter(tasks.withType<Test>())
    executionData.setFrom(fileTree(buildDir).include("/jacoco/*.exec"))
}

tasks.withType<JacocoCoverageVerification>().configureEach {
    dependsOn(tasks.withType<Test>())
    dependsOn(tasks.withType<JacocoReport>())
    executionData.setFrom(fileTree(buildDir).include("/jacoco/*.exec"))

    violationRules {
        rule {
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = 0.99.toBigDecimal()
            }
        }
        rule {
            limit {
                counter = "CLASS"
                value = "COVEREDRATIO"
                minimum = 1.0.toBigDecimal()
            }
        }
        rule {
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = 0.99.toBigDecimal()
            }
        }
        rule {
            limit {
                counter = "METHOD"
                value = "COVEREDRATIO"
                minimum = 0.99.toBigDecimal()
            }
        }
    }
}
