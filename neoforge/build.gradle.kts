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
    maven("https://maven.neoforged.net/releases")
}

val minecraftVersion = "1.21.6"

// NeoForge API versions for each Minecraft version for easier backporting
val neoforgeApiVersions = mapOf(
    "1.21.6" to "21.6.5-beta",
    "1.21.5" to "21.5.77",
    "1.21.4" to "21.4.138",
    "1.21.3" to "21.3.77",
    "1.21.2" to "21.2.1-beta",
    "1.21.1" to "21.1.180",
    "1.21" to "21.0.167",
    "1.20.6" to "20.6.135",
    "1.20.5" to "20.5.21-beta",
    "1.20.4" to "20.4.248",
    "1.20.3" to "20.3.8-beta",
    "1.20.2" to "20.2.93"
)

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(loom.officialMojangMappings())
    neoForge("net.neoforged:neoforge:${neoforgeApiVersions[minecraftVersion]}")
    api(projects.shared)
    compileOnly("net.luckperms:api:5.4")
}

loom.neoForge.accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

tasks {
    compileJava {
        options.release.set(21)
    }
}
