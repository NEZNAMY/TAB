dependencies {
    compileOnlyApi(libs.netty)
    api(libs.jsonSimple) {
        exclude("junit", "junit")
    }
    api(libs.snakeyaml)
    api(libs.yamlAssist)
    compileOnlyApi(libs.adventureMiniMessage)
    compileOnlyApi(libs.adventureApi)
    compileOnlyApi(libs.adventureLegacy)
    compileOnlyApi(libs.adventureGson)
    compileOnlyApi(libs.via)
}

tasks.javadoc {
    enabled = project.hasProperty("enable-javadoc")
}
