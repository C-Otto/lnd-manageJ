plugins {
    id("java")
}

testing {
    suites {
        named("test") {
            dependencies {
                testImplementation("com.github.valfirst:slf4j-test")
                configurations.named("testRuntimeOnly") {
                    exclude(group = "ch.qos.logback", module = "logback-classic")
                    exclude(group = "org.slf4j", module = "slf4j-nop")
                }
            }
        }
        withType<JvmTestSuite>().configureEach {
            dependencies {
                implementation(project.dependencies.platform("de.c-otto.lndmanagej:platform"))
                implementation("org.springframework.boot:spring-boot-starter-test")
            }
        }
    }
}