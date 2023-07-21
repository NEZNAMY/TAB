val minecraftVersion = "1.20.1"

val v114 = "0.28.5+1.14"
val v115 = "0.28.5+1.15"
val v116 = "0.42.0+1.16"

val fabricApiVersions = mapOf(
    "1.20.1" to "0.83.0+1.20.1",
    "1.20"   to "0.83.0+1.20",
    "1.19.4" to "0.77.0+1.19.4",
    "1.19.3" to "0.76.0+1.19.3",
    "1.19.2" to "0.76.0+1.19.2",
    "1.19.1" to "0.58.5+1.19.1",
    "1.19"   to "0.58.0+1.19",
    "1.18.2" to "0.76.0+1.18.2",
    "1.18.1" to "0.46.6+1.18",
    "1.18"   to "0.46.6+1.18",
    "1.17.1" to "0.46.1+1.17",
    "1.17"   to "0.46.1+1.17",
    "1.16.5" to v116,
    "1.16.4" to v116,
    "1.16.3" to v116,
    "1.16.2" to v116,
    "1.16.1" to v116,
    "1.16"   to v116,
    "1.15.2" to v115,
    "1.15.1" to v115,
    "1.15"   to v115,
    "1.14.4" to v114,
    "1.14.3" to v114,
    "1.14.2" to v114,
    "1.14.1" to v114,
    "1.14"   to v114,
)

plugins {
    id("fabric-loom")
}

repositories {
    // Gradle doesn't support combining settings and project repositories, so we have to re-declare all the settings repos we need
    maven("https://repo.kryptonmc.org/releases")
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.viaversion.com/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation(projects.shared)

    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc", "fabric-loader", "0.14.17")
    modImplementation("me.lucko", "fabric-permissions-api", "0.2-SNAPSHOT")
    modImplementation(fabricApi.module("fabric-api-base", fabricApiVersions[minecraftVersion]))
    modImplementation(fabricApi.module("fabric-lifecycle-events-v1", fabricApiVersions[minecraftVersion]))
    modImplementation(fabricApi.module("fabric-networking-api-v1", fabricApiVersions[minecraftVersion]))
    modImplementation(fabricApi.module("fabric-entity-events-v1", fabricApiVersions[minecraftVersion]))
    modImplementation(fabricApi.module("fabric-command-api-v${if (minecraftVersion.split(".")[1].toInt() >= 19) "2" else "1"}", fabricApiVersions[minecraftVersion]))
}

loom {
    accessWidenerPath.set(file("src/main/resources/tab.accesswidener"))
}

tasks {
    compileJava {
        options.release.set(17)
    }
    validateAccessWidener {
        // We don't want to validate the access wideners, as we have wideners for multiple versions, and the validation task will
        // fail, as the classes for the old wideners don't exist in the new versions and vice versa.
        enabled = false
    }
}
