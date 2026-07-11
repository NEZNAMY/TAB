dependencies {
    implementation(projects.shared)
    compileOnly("io.fand:fand-api:0.7.4+build.1")
}

tasks.compileJava {
    options.release.set(25)
}
