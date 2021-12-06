plugins {
    kotlin("jvm") version "1.6.0"
    kotlin("kapt") version "1.6.0"
}

dependencies {
    implementation(projects.shared)
    compileOnly(libs.krypton.api)
    compileOnly(libs.krypton.server) {
        exclude("ca.spottedleaf", "dataconverter")
    }
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.via)
    kapt(libs.krypton.annotationProcessor)
}

tasks.compileKotlin {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xjvm-default=enable")
    }
}
