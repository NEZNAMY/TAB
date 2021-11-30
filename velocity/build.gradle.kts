dependencies {
    implementation(projects.shared)
    implementation(libs.bstats.velocity)
    compileOnly(libs.velocityInjector)
    compileOnly(libs.velocity)
    annotationProcessor(libs.velocity)
}
