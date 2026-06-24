import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("dev.architectury.loom-no-remap")
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

val minecraftVersion = "26.2"

// Forge API versions for each Minecraft version for easier backporting
// Official website (for updating in the future): https://files.minecraftforge.net/net/minecraftforge/forge/
val forgeApiVersions = mapOf(
    "26.2" to "26.2-65.0.0",
    "26.1.2" to "26.1.2-64.0.4"
)

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    forge("net.minecraftforge:forge:${forgeApiVersions[minecraftVersion]}")
    api(projects.shared)
    compileOnly("net.luckperms:api:5.5")
}

loom.forge.accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

tasks {
    compileJava {
        options.release.set(25)
    }
    withType<ShadowJar>().configureEach {
        enabled = false
    }
}
