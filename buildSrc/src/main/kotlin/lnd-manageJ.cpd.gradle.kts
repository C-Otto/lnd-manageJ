import de.aaschmid.gradle.plugins.cpd.Cpd

plugins {
    id("de.aaschmid.cpd")
}

tasks.withType<Test>{
    shouldRunAfter(tasks.withType<Cpd>())
}
