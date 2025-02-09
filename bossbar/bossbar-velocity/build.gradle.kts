dependencies {
    implementation(projects.bossbar.bossbarShared)
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
}

tasks.compileJava {
    options.release.set(17)
}