plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.16"
}

repositories {
    maven("https://jitpack.io") // YamlAssist
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.viaversion.com/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

val version = "1.21.5-R0.1-SNAPSHOT"

dependencies {
    implementation(projects.bukkit)
    paperweight.paperDevBundle("1.21.5-no-moonrise-SNAPSHOT") // Just temporary
    compileOnly("io.papermc.paper:paper-api:${version}")
}

tasks.compileJava {
    options.release.set(21)
}