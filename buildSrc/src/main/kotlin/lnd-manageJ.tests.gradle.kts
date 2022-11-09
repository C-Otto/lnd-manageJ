import com.adarshr.gradle.testlogger.theme.ThemeType

plugins {
    id("java")
    id("com.adarshr.test-logger")
}


testing {
    suites {
        named("test") {
            dependencies {
                testImplementation("uk.org.lidalia:slf4j-test")
                configurations.named("testRuntimeOnly") {
                    exclude(group = "ch.qos.logback", module = "logback-classic")
                    exclude(group = "org.slf4j", module = "slf4j-nop")
                }
            }
        }
        withType<JvmTestSuite>().configureEach {
            useJUnitJupiter()
            dependencies {
                implementation("nl.jqno.equalsverifier:equalsverifier")
                implementation("org.springframework.boot:spring-boot-starter-test")
            }
        }
    }
}


tasks.withType<Test>().configureEach {
    afterTest(KotlinClosure2<TestDescriptor, TestResult, Unit>({ _, result ->
        if (result.resultType == TestResult.ResultType.SKIPPED) {
            throw GradleException("Do not ignore test cases")
        }
    }))
    systemProperties = mapOf("junit.jupiter.displayname.generator.default" to "org.junit.jupiter.api.DisplayNameGenerator\$ReplaceUnderscores")
}

testlogger {
    theme = ThemeType.STANDARD_PARALLEL
    slowThreshold = 1000
    showSimpleNames = true
    showOnlySlow = true
}
