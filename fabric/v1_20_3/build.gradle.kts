plugins {
    id("fabric-loom")
}

repositories {
    // Gradle doesn't support combining settings and project repositories, so we have to re-declare all the settings repos we need
    maven("https://jitpack.io") // YamlAssist
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.viaversion.com/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation(projects.fabric)
    minecraft("com.mojang:minecraft:1.20.3")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.14.17")
    val fabricApiVersion = "0.90.4+1.20.3"
    modImplementation(fabricApi.module("fabric-api-base", fabricApiVersion))
    modImplementation(fabricApi.module("fabric-command-api-v2", fabricApiVersion))
}

tasks {
    compileJava {
        options.release.set(17)
    }
}
