import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("dev.architectury.loom")
}

repositories {
    // Gradle doesn't support combining settings and project repositories, so we have to re-declare all the settings repos we need
    maven("https://jitpack.io") // YamlAssist
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.viaversion.com/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://maven.nucleoid.xyz/")
    maven("https://repo.papermc.io/repository/maven-public/") // Adventure
}

val minecraftVersion = "1.21.10"

// Fabric API versions for each Minecraft version for easier backporting
// Official website (for updating in the future): https://fabricmc.net/develop/
val fabricApiVersions = mapOf(
    "1.21.10" to "0.136.0+1.21.10",
    "1.21.9" to "0.134.0+1.21.9",
    "1.21.8" to "0.136.0+1.21.8",
    "1.21.7" to "0.129.0+1.21.7",
    "1.21.6" to "0.128.2+1.21.6",
    "1.21.5" to "0.128.2+1.21.5",
    "1.21.4" to "0.119.4+1.21.4",
    "1.21.3" to "0.114.1+1.21.3",
    "1.21.2" to "0.106.1+1.21.2",
    "1.21.1" to "0.116.6+1.21.1",
    "1.21" to "0.102.0+1.21",
    "1.20.6" to "0.100.8+1.20.6",
    "1.20.5" to "0.97.8+1.20.5",
    "1.20.4" to "0.97.3+1.20.4",
    "1.20.3" to "0.91.1+1.20.3",
    "1.20.2" to "0.91.6+1.20.2",
    "1.20.1" to "0.92.6+1.20.1",
    "1.20" to "0.83.0+1.20",
    "1.19.4" to "0.87.2+1.19.4",
    "1.19.3" to "0.76.1+1.19.3",
    "1.19.2" to "0.77.0+1.19.2",
    "1.19.1" to "0.58.5+1.19.1",
    "1.19" to "0.58.0+1.19",
    "1.18.2" to "0.77.0+1.18.2",
    "1.18.1" to "0.46.6+1.18",
    "1.18" to "0.44.0+1.18",
    "1.17.1" to "0.46.1+1.17",
    "1.17" to "0.46.1+1.17",
    "1.16.5" to "0.42.0+1.16",
    "1.16.4" to "0.42.0+1.16",
    "1.16.3" to "0.42.0+1.16",
    "1.16.2" to "0.42.0+1.16",
    "1.16.1" to "0.42.0+1.16",
    "1.16" to "0.42.0+1.16"
)

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(loom.officialMojangMappings())
    api(projects.shared)
    modImplementation("me.lucko:fabric-permissions-api:0.2-SNAPSHOT")
    modImplementation("eu.pb4:placeholder-api:2.5.0+1.21.2")
    modImplementation("net.fabricmc:fabric-loader:0.17.3")
    modImplementation(fabricApi.module("fabric-lifecycle-events-v1", fabricApiVersions[minecraftVersion]))
    modImplementation(fabricApi.module("fabric-networking-api-v1", fabricApiVersions[minecraftVersion]))
    modImplementation(fabricApi.module("fabric-entity-events-v1", fabricApiVersions[minecraftVersion]))
    modImplementation(fabricApi.module("fabric-command-api-v2", "0.58.0+1.19"))
    modImplementation(fabricApi.module("fabric-command-api-v1", "0.77.0+1.18.2"))
}

loom.accessWidenerPath.set(file("src/main/resources/resources/tab.accesswidener"))

tasks {
    compileJava {
        options.release.set(21)
    }
    validateAccessWidener {
        enabled = true
    }
    withType<ShadowJar>().configureEach {
        enabled = false
    }
}
