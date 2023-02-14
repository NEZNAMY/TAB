import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "1.8.0"
    kotlin("kapt") version "1.8.0"
}

dependencies {
    implementation(projects.shared)
    compileOnly(libs.krypton.api)
    compileOnly(libs.krypton.server) {
        exclude("ca.spottedleaf", "dataconverter")
        exclude("org.kryptonmc", "nbt-common")
        exclude("org.kryptonmc", "nbt-extra-kotlin")
        exclude("org.kryptonmc", "serialization-core")
        exclude("org.kryptonmc", "serialization-gson")
        exclude("org.kryptonmc", "serialization-nbt")
    }
    compileOnly(libs.kotlin.stdlib)
    kapt(libs.krypton.annotationProcessor)
}

tasks {
    compileKotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }
}
