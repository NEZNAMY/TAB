dependencies {
    implementation(projects.shared)
    compileOnly(libs.krypton.api)
    compileOnly(libs.spark)
    annotationProcessor(libs.krypton.annotationProcessor)
}

tasks.compileJava {
    options.release.set(17)
}
