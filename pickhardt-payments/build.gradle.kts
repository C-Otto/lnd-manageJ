plugins {
    id("lnd-manageJ.java-library-conventions")
}

dependencies {
    implementation(project(":backend"))
    implementation(project(":caching"))
    implementation(project(":configuration"))
    implementation(project(":model"))
    implementation(project(":grpc-adapter"))
    implementation("com.google.ortools:ortools-java")
    implementation("org.eclipse.collections:eclipse-collections")
    implementation("org.reactivestreams:reactive-streams")
    testImplementation(testFixtures(project(":model")))
    testImplementation("org.awaitility:awaitility")
    integrationTestImplementation(project(":backend"))
    integrationTestImplementation(project(":grpc-adapter"))
    integrationTestImplementation(testFixtures(project(":model")))
    testFixturesImplementation(testFixtures(project(":model")))
}

var deleteCrashLogs = tasks.register<Delete>("deleteCrashLogs") {
    delete(fileTree(project.projectDir).matching {
        include("hs_err_pid*.log")
    })
}
tasks.named("pitest") {
    finalizedBy(deleteCrashLogs)
}

pitest {
    testStrengthThreshold.set(98)
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rules.forEach {rule ->
            rule.limits.forEach {limit ->
                if (limit.counter == "BRANCH") {
                    limit.minimum = 0.98.toBigDecimal()
                }
            }
        }
    }
}
