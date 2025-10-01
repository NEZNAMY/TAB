plugins {
    id("net.kyori.blossom") version "2.1.0"
}

dependencies {
    api(projects.api)
    api("org.yaml:snakeyaml:2.0")
    api("com.github.NEZNAMY:yamlassist:1.0.8")
    api("com.googlecode.json-simple:json-simple:1.1.1") {
        exclude("junit", "junit")
    }
    api("net.kyori:event-method:3.0.0") {
        exclude("com.google.guava", "guava")
        exclude("org.checkerframework", "checker-qual")
    }
    compileOnlyApi("com.viaversion:viaversion-api:5.2.1")
    compileOnlyApi("io.netty:netty-all:4.1.90.Final")
    compileOnlyApi("net.luckperms:api:5.4")
    compileOnlyApi("com.google.guava:guava:31.1-jre")
    compileOnlyApi("org.geysermc.floodgate:api:2.2.0-SNAPSHOT")
    compileOnlyApi("net.kyori:adventure-api:4.25.0-SNAPSHOT")
    compileOnlyApi("net.kyori:adventure-text-minimessage:4.25.0-SNAPSHOT")
    implementation("com.saicone.delivery4j:delivery4j:1.1.1")
    implementation("com.saicone.delivery4j:broker-rabbitmq:1.1.1") {
        exclude("org.slf4j", "slf4j-api")
    }
    implementation("com.saicone.delivery4j:broker-redis:1.1.1") {
        exclude("com.google.code.gson", "gson")
        exclude("org.slf4j", "slf4j-api")
    }
    implementation("com.saicone.delivery4j:extension-guava:1.1.1")
}

sourceSets.main {
    blossom {
        javaSources {
            property("name", rootProject.name)
            property("id", rootProject.ext.get("id")!!.toString())
            property("version", project.version.toString())
            property("description", project.description)
            property("website", rootProject.ext.get("website")!!.toString())
            property("author", rootProject.ext.get("author")!!.toString())
            property("credits", rootProject.ext.get("credits")!!.toString())
        }
    }
}