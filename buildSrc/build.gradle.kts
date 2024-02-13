plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(platform("de.c-otto.lndmanagej:platform"))
    implementation("de.c-otto:java-conventions:2024.02.13")
    implementation("org.springframework.boot:spring-boot-gradle-plugin")
    implementation("com.google.protobuf:protobuf-gradle-plugin")
}
