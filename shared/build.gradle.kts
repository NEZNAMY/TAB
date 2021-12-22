plugins {
    id("net.kyori.blossom") version "1.2.0"
}

dependencies {
    api(projects.api)
    api(libs.event) {
        exclude("com.google.guava", "guava")
        exclude("org.checkerframework", "checker-qual")
    }
    compileOnlyApi(libs.luckperms)
    compileOnlyApi(libs.guava)
    compileOnlyApi(libs.floodgate)
}

blossom {
    replaceToken("@plugin_version@", project.version)
    replaceTokenIn("src/main/java/me/neznamy/tab/shared/TAB.java")
}
