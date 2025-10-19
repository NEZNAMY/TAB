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
    maven("https://maven.neoforged.net/releases")
    maven("https://repo.papermc.io/repository/maven-public/") // Adventure
}

val minecraftVersion = "1.21.10"

// NeoForge API versions for each Minecraft version for easier backporting
// Official website (for updating in the future): https://projects.neoforged.net/neoforged/neoforge
val neoforgeApiVersions = mapOf(
    "1.21.10" to "21.10.0-beta",
    "1.21.9" to "21.9.16-beta",
    "1.21.8" to "21.8.47",
    "1.21.7" to "21.7.25-beta",
    "1.21.6" to "21.6.20-beta",
    "1.21.5" to "21.5.95",
    "1.21.4" to "21.4.154",
    "1.21.3" to "21.3.93",
    "1.21.2" to "21.2.1-beta",
    "1.21.1" to "21.1.209",
    "1.21" to "21.0.167",
    "1.20.6" to "20.6.138",
    "1.20.5" to "20.5.21-beta",
    "1.20.4" to "20.4.250",
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
    withType<ShadowJar>().configureEach {
        enabled = false
    }
}
