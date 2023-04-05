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

// Supported versions: 1.19.3, 1.19.4
val minecraftVersion = "1.19.4"

dependencies {
    implementation(projects.shared)

    minecraft("com.mojang", "minecraft", minecraftVersion)
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc", "fabric-loader", "0.14.17")
    modImplementation("me.lucko", "fabric-permissions-api", "0.2-SNAPSHOT")
    val apiModules = setOf("fabric-api-base", "fabric-command-api-v2", "fabric-lifecycle-events-v1", "fabric-networking-api-v1")
    apiModules.forEach { modImplementation(fabricApi.module(it, "0.73.0+$minecraftVersion")) }
}

tasks.compileJava {
    options.release.set(17)
}
