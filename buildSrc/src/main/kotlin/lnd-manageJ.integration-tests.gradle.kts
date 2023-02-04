plugins {
    id("de.c-otto.java-conventions")
}

testing {
    suites {
        named("integrationTest", JvmTestSuite::class).configure {
            dependencies {
                implementation("com.tngtech.archunit:archunit")
                implementation("org.awaitility:awaitility")
            }
        }
    }
}