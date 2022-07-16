plugins {
    id("net.kyori.blossom") version "1.2.0"
}

dependencies {
    compileOnlyApi(libs.netty)
    api(libs.jsonSimple) {
        exclude("junit", "junit")
    }
    compileOnlyApi(libs.snakeyaml)
    api(libs.yamlAssist)
    compileOnlyApi(libs.gson)
    compileOnlyApi(libs.adventureMiniMessage)
    compileOnlyApi(libs.adventureApi)
    compileOnlyApi(libs.adventureLegacy)
}

blossom {
    replaceToken("@plugin_version@", project.version)
    replaceTokenIn("src/main/java/me/neznamy/tab/api/TabConstants.java")
}