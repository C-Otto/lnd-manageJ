plugins {
    id("lnd-manageJ.java-library-conventions")
}

dependencies {
    api("com.github.ben-manes.caffeine:caffeine")
}

pitest {
    testStrengthThreshold.set(91)
}
