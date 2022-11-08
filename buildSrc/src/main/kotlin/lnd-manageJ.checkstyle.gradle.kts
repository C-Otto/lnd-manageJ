plugins {
    checkstyle
}

checkstyle {
    maxWarnings = 0
}

tasks.withType<Test>{
    shouldRunAfter(tasks.withType<Checkstyle>())
}
