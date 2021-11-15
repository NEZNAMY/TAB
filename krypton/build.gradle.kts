plugins {
    kotlin("jvm") version "1.6.0-RC2"
    kotlin("kapt") version "1.6.0-RC2"
}

dependencies {
    implementation(projects.shared)
    compileOnly(libs.krypton.api)
    compileOnly(libs.krypton.server)
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.via)
    compileOnly(libs.spark)
}

tasks.compileKotlin {
    kotlinOptions {
        jvmTarget = "16"
        freeCompilerArgs = listOf("-Xjvm-default=enable")
    }
}
