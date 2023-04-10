dependencies {
    compileOnlyApi("org.jetbrains:annotations:24.0.1")
    compileOnlyApi("com.viaversion:viaversion-api:4.5.1")
}

tasks.javadoc {
    enabled = project.hasProperty("enable-javadoc")
}
