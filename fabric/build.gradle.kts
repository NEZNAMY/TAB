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
    maven("https://repo.papermc.io/repository/maven-public/") // Adventure
}

val minecraftVersion = "26.2"

// Fabric API versions for each Minecraft version for easier backporting
// Official website (for updating in the future): https://fabricmc.net/develop/
val fabricApiVersions = mapOf(
    "26.2" to "0.150.3+26.2",
    "26.1.2" to "0.150.0+26.1.2"
)

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    api(projects.shared)
    implementation("me.lucko:fabric-permissions-api:0.7.0")
    implementation("eu.pb4:placeholder-api:3.0.0+26.1")
    implementation("net.fabricmc:fabric-loader:0.19.2")
    implementation(fabricApi.module("fabric-lifecycle-events-v1", fabricApiVersions.getValue(minecraftVersion)))
    implementation(fabricApi.module("fabric-networking-api-v1", fabricApiVersions.getValue(minecraftVersion)))
    implementation(fabricApi.module("fabric-entity-events-v1", fabricApiVersions.getValue(minecraftVersion)))
    implementation(fabricApi.module("fabric-command-api-v2", fabricApiVersions.getValue(minecraftVersion)))
}

loom.accessWidenerPath.set(file("src/main/resources/resources/tab.accesswidener"))

tasks {
    compileJava {
        options.release.set(25)
    }
    validateAccessWidener {
        enabled = true
    }
    withType<ShadowJar>().configureEach {
        enabled = false
    }
}
