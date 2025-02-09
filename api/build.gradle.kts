dependencies {
    compileOnlyApi("org.jetbrains:annotations:26.0.1")
}

tasks.javadoc {
    enabled = project.hasProperty("enable-javadoc")
}
