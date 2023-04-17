dependencies {
    compileOnlyApi("org.jetbrains:annotations:24.0.1")
}

tasks.javadoc {
    enabled = project.hasProperty("enable-javadoc")
}
