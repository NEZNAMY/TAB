plugins {
    id("net.kyori.blossom") version "1.3.1"
}

dependencies {
    api(projects.api)
    api(projects.component)
    api("org.yaml:snakeyaml:2.0")
    api("com.github.NEZNAMY:yamlassist:1.0.8")
    api("com.googlecode.json-simple:json-simple:1.1.1") {
        exclude("junit", "junit")
    }
    api("net.kyori:event-method:3.0.0") {
        exclude("com.google.guava", "guava")
        exclude("org.checkerframework", "checker-qual")
    }
    compileOnlyApi("com.viaversion:viaversion-api:4.5.1")
    compileOnlyApi("io.netty:netty-all:4.1.90.Final")
    compileOnlyApi("net.luckperms:api:5.4")
    compileOnlyApi("com.google.guava:guava:31.1-jre")
    compileOnlyApi("org.geysermc.floodgate:api:2.2.0-SNAPSHOT")
    compileOnlyApi("net.kyori:adventure-api:4.18.0")
    compileOnlyApi("net.kyori:adventure-text-minimessage:4.18.0")
    implementation("com.saicone.delivery4j:delivery4j:1.1.1")
    implementation("com.saicone.delivery4j:broker-rabbitmq:1.1.1")
    implementation("com.saicone.delivery4j:broker-redis:1.1.1")
    implementation("com.saicone.delivery4j:extension-guava:1.1.1")
    implementation("org.slf4j:slf4j-nop:1.7.36")
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