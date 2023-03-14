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
    compileOnlyApi(libs.adventureMiniMessage)
    compileOnlyApi(libs.adventureApi)
    compileOnlyApi(libs.adventureLegacy)
    compileOnlyApi(libs.via)
}

blossom {
    replaceToken("@name@", rootProject.name)
    replaceToken("@id@", rootProject.ext.get("id")!!.toString())
    replaceToken("@version@", project.version)
    replaceToken("@description@", project.description)
    replaceToken("@website@", rootProject.ext.get("website")!!.toString())
    replaceToken("@author@", rootProject.ext.get("author")!!.toString())
    replaceTokenIn("src/main/java/me/neznamy/tab/api/TabConstants.java")
}

tasks.javadoc {
    enabled = project.hasProperty("enable-javadoc")
}
