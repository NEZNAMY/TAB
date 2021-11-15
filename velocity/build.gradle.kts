dependencies {
    implementation(projects.shared)
    implementation(libs.bstats.velocity)
    compileOnly(libs.velocity)
    annotationProcessor(libs.velocity)
}
