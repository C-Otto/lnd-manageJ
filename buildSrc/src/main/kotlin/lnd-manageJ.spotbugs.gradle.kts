import com.github.spotbugs.snom.SpotBugsTask

plugins {
    id("com.github.spotbugs")
}

tasks.withType<SpotBugsTask>().configureEach {
    reports.create("xml") {
        enabled = false
    }
    reports.create("html") {
        enabled = true
    }
}

spotbugs {
    excludeFilter.set(file("${rootDir}/config/spotbugs-exclude.xml"))
}


tasks.withType<Test>().forEach { testTask ->
    tasks.withType<SpotBugsTask>().forEach { spotbugsTask ->
        val expected = ("spotbugs" + testTask.getName()).toLowerCase()
        if (expected == spotbugsTask.getName().toLowerCase()) {
            testTask.shouldRunAfter(spotbugsTask)
        }
        if (spotbugsTask.getName() == "spotbugsMain") {
            testTask.shouldRunAfter(spotbugsTask)
        }
    }
}
