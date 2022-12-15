plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    repositories {
        maven {
            url = uri("https://repo.spring.io/milestone")
        }
    }
}

dependencies {
    implementation(platform("de.cotto.lndmanagej:platform"))
    implementation("de.aaschmid:gradle-cpd-plugin")
    implementation("net.ltgt.gradle:gradle-errorprone-plugin")
    implementation("net.ltgt.gradle:gradle-nullaway-plugin")
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin")
    implementation("org.springframework.boot:spring-boot-gradle-plugin")
    implementation("com.adarshr:gradle-test-logger-plugin")
    implementation("info.solidsoft.gradle.pitest:gradle-pitest-plugin")
    implementation("com.google.protobuf:protobuf-gradle-plugin")
}
