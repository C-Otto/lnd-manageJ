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

abstract class CheckForExecutionDataTask : DefaultTask() {
    @InputFiles
    val executionData = project.files()

    @TaskAction
    fun check() {
        if (executionData.isEmpty) {
            throw GradleException("No tests found for " + this.project)
        }
    }
}

tasks.register<CheckForExecutionDataTask>("checkForExecutionData") {
    mustRunAfter(tasks.withType<Test>())
    executionData.setFrom(fileTree(buildDir).include("/jacoco/*.exec"))
}

tasks.withType<JacocoCoverageVerification>().configureEach {
    dependsOn(tasks.withType<Test>())
    dependsOn(tasks.withType<JacocoReport>())
    dependsOn(tasks.withType<CheckForExecutionDataTask>())
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
