import com.github.spotbugs.snom.SpotBugsExtension
import de.aaschmid.gradle.plugins.cpd.CpdExtension
import info.solidsoft.gradle.pitest.PitestPluginExtension
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.plugins.quality.PmdExtension
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension

configure<CheckstyleExtension> {
    toolVersion = "10.4"
}

configure<CpdExtension> {
    toolVersion = "6.52.0"
}

configure<PmdExtension> {
    toolVersion = "6.52.0"
}

configure<JacocoPluginExtension> {
    toolVersion = "0.8.8"
}

configure<PitestPluginExtension> {
    pitestVersion.set("1.9.11")
    junit5PluginVersion.set("1.1.0")
}

configure<SpotBugsExtension> {
    toolVersion.set("4.7.3")
}
