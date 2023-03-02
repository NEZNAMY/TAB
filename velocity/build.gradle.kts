dependencies {
    implementation(projects.shared)
    implementation(libs.bstats.velocity)
    compileOnly(libs.velocity) {
        exclude("com.velocitypowered", "velocity-brigadier") // Breaks compilation due to requiring Java 11
    }
    annotationProcessor(libs.velocity)
}
