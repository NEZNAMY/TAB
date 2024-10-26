plugins {
    id("fabric-loom")
}

repositories {
    // Gradle doesn't support combining settings and project repositories, so we have to re-declare all the settings repos we need
    maven("https://jitpack.io") // YamlAssist
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.viaversion.com/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://maven.nucleoid.xyz/")
}

dependencies {
    implementation(projects.fabric)
    minecraft("com.mojang:minecraft:1.18.2")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.15.9") // Not required, but causes warn if not present
}

tasks {
    compileJava {
        options.release.set(17)
    }
}
