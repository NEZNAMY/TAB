dependencies {
    compileOnlyApi("org.jetbrains:annotations:26.0.2")
}

tasks.javadoc {
    enabled = project.hasProperty("enable-javadoc")
}
