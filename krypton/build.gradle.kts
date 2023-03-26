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
    compileOnly(libs.spark)
    annotationProcessor(libs.krypton.annotationProcessor)
}
tasks.compileJava {
    options.release.set(17)
}