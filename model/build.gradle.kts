plugins {
    id("lnd-manageJ.java-library-conventions")
}

dependencies {
    testImplementation("org.awaitility:awaitility")
}

pitest {
    testStrengthThreshold.set(99)
}
