plugins {
    id("pmd")
}

pmd {
    ruleSetFiles = files("${rootDir}/config/pmd-ruleset.xml")
    ruleSets = listOf()
    isConsoleOutput = true
}

tasks.withType<Test>().forEach { testTask ->
    tasks.withType<Pmd>().forEach { pmdTask ->
        val expected = ("pmd" + testTask.getName()).toLowerCase()
        if (expected == pmdTask.getName().toLowerCase()) {
            testTask.shouldRunAfter(pmdTask)
        }
        if (pmdTask.getName() == "pmdMain") {
            testTask.shouldRunAfter(pmdTask)
        }
    }
}

tasks.withType<Test>{
    shouldRunAfter(tasks.withType<Pmd>())
}
