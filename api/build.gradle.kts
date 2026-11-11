dependencies {
    compileOnlyApi("org.jetbrains:annotations:26.1.0")
}

tasks.javadoc {
    enabled = project.hasProperty("enable-javadoc")
}
