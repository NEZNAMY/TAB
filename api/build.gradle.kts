dependencies {
    compileOnlyApi(libs.netty)
    api(libs.jsonSimple) {
        exclude("junit", "junit")
    }
    compileOnlyApi(libs.snakeyaml)
    api(libs.yamlAssist)
    compileOnlyApi(libs.gson)
}
