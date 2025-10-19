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

// Forge API versions for each Minecraft version for easier backporting
// Official website (for updating in the future): https://files.minecraftforge.net/net/minecraftforge/forge/
val forgeApiVersions = mapOf(
    "1.21.10" to "1.21.10-60.0.0",
    "1.21.9" to "1.21.9-59.0.0",
    "1.21.8" to "1.21.8-58.1.6",
    "1.21.7" to "1.21.7-57.0.3",
    "1.21.6" to "1.21.6-56.0.9",
    "1.21.5" to "1.21.5-55.1.2",
    "1.21.4" to "1.21.4-54.1.8",
    "1.21.3" to "1.21.3-53.1.4",
    "1.21.2" to null,
    "1.21.1" to "1.21.1-52.1.5",
    "1.21" to "1.21-51.0.33",
    "1.20.6" to "1.20.6-50.2.2",
    "1.20.5" to null,
    "1.20.4" to "1.20.4-49.2.2",
    "1.20.3" to "1.20.3-49.0.2", // Broken dependencies
    "1.20.2" to "1.20.2-48.1.0",
    "1.20.1" to "1.20.1-47.4.9",
    "1.20" to "1.20-46.0.14",
    "1.19.4" to "1.19.4-45.4.2",
    "1.19.3" to "1.19.3-44.1.23",
    "1.19.2" to "1.19.2-43.5.1",
    "1.19.1" to "1.19.1-42.0.9",
    "1.19" to "1.19-41.1.0",
    "1.18.2" to "1.18.2-40.3.11",
    "1.18.1" to "1.18.1-39.1.2",
    "1.18" to "1.18-38.0.17",
    "1.17.1" to "1.17.1-37.1.1",
    "1.17" to null,
    "1.16.5" to "1.16.5-36.2.42",
    "1.16.4" to "1.16.4-35.1.37",
    "1.16.3" to "1.16.3-34.1.42",
    "1.16.2" to "1.16.2-33.0.61",
    "1.16.1" to "1.16.1-32.0.108", // Broken version
    "1.16" to null,
    // No one will most likely want anything older, lets stop here
)

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(loom.officialMojangMappings())
    forge("net.minecraftforge:forge:${forgeApiVersions[minecraftVersion]}")
    api(projects.shared)
    compileOnly("net.luckperms:api:5.4")
}

loom.forge.accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

tasks {
    compileJava {
        options.release.set(21)
    }
    withType<ShadowJar>().configureEach {
        enabled = false
    }
}
