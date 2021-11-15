plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("gradle.plugin.com.github.johnrengelman", "shadow", "7.1.0")
    implementation("org.sonarsource.scanner.gradle", "sonarqube-gradle-plugin", "3.3")
}
