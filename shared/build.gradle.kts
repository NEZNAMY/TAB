plugins {
    id("net.kyori.blossom") version "1.2.0"
}

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

blossom {
    replaceToken("@name@", rootProject.name)
    replaceToken("@id@", rootProject.ext.get("id")!!.toString())
    replaceToken("@version@", project.version)
    replaceToken("@description@", project.description)
    replaceToken("@website@", rootProject.ext.get("website")!!.toString())
    replaceToken("@author@", rootProject.ext.get("author")!!.toString())
    replaceTokenIn("src/main/java/me/neznamy/tab/shared/TabConstants.java")
}