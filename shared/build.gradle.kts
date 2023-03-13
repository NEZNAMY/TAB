dependencies {
    api(projects.api)
    api(libs.event) {
        exclude("com.google.guava", "guava")
        exclude("org.checkerframework", "checker-qual")
    }
    compileOnlyApi(libs.netty)
    compileOnlyApi(libs.luckperms)
    compileOnlyApi(libs.guava)
    compileOnlyApi(libs.floodgate)
    compileOnlyApi(libs.slf4j)
}