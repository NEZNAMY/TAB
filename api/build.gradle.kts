dependencies {
    compileOnlyApi("org.jetbrains:annotations:24.1.0")
}

tasks.javadoc {
    enabled = project.hasProperty("enable-javadoc")
}
