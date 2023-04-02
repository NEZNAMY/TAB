plugins {
    id("fabric-loom")
}

repositories {
    // Gradle doesn't support combining settings and project repositories, so we have to re-declare all the settings repos we need
    maven("https://repo.kryptonmc.org/releases")
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.viaversion.com/")
}

dependencies {
    implementation(projects.shared)

    minecraft("com.mojang:minecraft:1.19.4")
    mappings("net.fabricmc:yarn:1.19.4+build.1:v2")
    modImplementation("net.fabricmc:fabric-loader:0.14.17")
    val apiModules = setOf("fabric-api-base", "fabric-command-api-v2", "fabric-lifecycle-events-v1", "fabric-networking-api-v1")
    apiModules.forEach { modImplementation(fabricApi.module(it, "0.73.0+1.19.4")) }
}

tasks.compileJava {
    options.release.set(17)
}
