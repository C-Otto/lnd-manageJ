plugins {
    id("java-library")
    id("lnd-manageJ.java-conventions")
}

tasks.bootJar {
    enabled = false
    archiveClassifier.set("boot")
}
tasks.jar {
    enabled = true
}
