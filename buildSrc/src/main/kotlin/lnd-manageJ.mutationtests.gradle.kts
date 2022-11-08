import info.solidsoft.gradle.pitest.PitestTask
import kotlin.math.max

plugins {
    id("lnd-manageJ.tests")
    id("info.solidsoft.pitest")
}

pitest {
    targetClasses.set(listOf("de.cotto.*"))
    outputFormats.set(listOf("XML", "HTML"))
    timestampedReports.set(false)
    failWhenNoMutations.set(false)
    excludedMethods.set(listOf("hashCode"))
    threads.set(max(Runtime.getRuntime().availableProcessors() / 2, 1))
    testStrengthThreshold.set(100)
}

tasks.withType<PitestTask>().configureEach {
    dependsOn(tasks.named("test"))
}
