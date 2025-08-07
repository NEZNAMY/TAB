plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("com.gradleup.shadow:shadow-gradle-plugin:8.3.9")
    implementation("io.freefair.gradle:lombok-plugin:8.14")
    implementation("dev.architectury.loom:dev.architectury.loom.gradle.plugin:1.10-SNAPSHOT")  // Used for NeoForge/Forge support
}
