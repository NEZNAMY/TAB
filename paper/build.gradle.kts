plugins {
    id("io.papermc.paperweight.userdev") version "1.7.1"
}

repositories {
    maven("https://jitpack.io") // YamlAssist
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.viaversion.com/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation(projects.bukkit)
    implementation(projects.shared) // No idea why is this needed when Bukkit already contains it
    paperweight.paperDevBundle("1.20.6-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
}

tasks.compileJava {
    options.release.set(21)
}