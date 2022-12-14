import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import net.ltgt.gradle.nullaway.nullaway

plugins {
    id("net.ltgt.errorprone")
    id("net.ltgt.nullaway")
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone.disable("EqualsGetClass")
    options.errorprone.excludedPaths.set(".*/generated/.*")
    options.errorprone.nullaway {
        excludedFieldAnnotations.add("org.mockito.Mock")
        excludedFieldAnnotations.add("org.mockito.InjectMocks")
        excludedFieldAnnotations.add("org.junit.jupiter.api.io.TempDir")
        excludedFieldAnnotations.add("org.springframework.boot.test.mock.mockito.MockBean")
        excludedFieldAnnotations.add("org.springframework.beans.factory.annotation.Autowired")
        excludedClassAnnotations.add("org.springframework.boot.context.properties.ConfigurationProperties")
        excludedFieldAnnotations.add("org.mockito.Captor")
        excludedFieldAnnotations.add("org.springframework.beans.factory.annotation.Value")
        severity.set(CheckSeverity.ERROR)
    }
}

dependencies {
    errorprone(platform("de.cotto.lndmanagej:platform"))
    errorprone("com.google.errorprone:error_prone_core")
    errorprone("com.uber.nullaway:nullaway")
}

nullaway {
    annotatedPackages.add("de.cotto")
}
