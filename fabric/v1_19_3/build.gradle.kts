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
    minecraft("com.mojang:minecraft:1.19.3")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.15.9") // Not required, but causes warn if not present
}

loom {
    accessWidenerPath.set(file("../src/main/resources/resources/tab.accesswidener"))
}

tasks {
    compileJava {
        options.release.set(17)
    }

    validateAccessWidener {
        enabled = false // Disable validation because the file is made for latest version, so some fields will throw error (the one we need has not changed)
    }
}
