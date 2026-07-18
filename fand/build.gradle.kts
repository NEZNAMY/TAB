dependencies {
    implementation(projects.shared)
    compileOnly("io.fand:fand-api:0.8.3+build.1")
}

tasks.compileJava {
    options.release.set(25)
}
