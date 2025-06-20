plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
}

repositories {
    maven("https://jitpack.io") // YamlAssist
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.viaversion.com/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

val version = "1.21.3-R0.1-SNAPSHOT" // 1.21.2 causes gradle error, but the versions are interchangeable

dependencies {
    implementation(projects.bukkit)
    paperweight.paperDevBundle(version)
    compileOnly("io.papermc.paper:paper-api:${version}")
}

tasks.compileJava {
    options.release.set(21)
}