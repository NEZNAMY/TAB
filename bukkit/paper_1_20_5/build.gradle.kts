plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

repositories {
    maven("https://jitpack.io") // YamlAssist
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.viaversion.com/")
    maven {
        name = "luck-repo"
        url = uri("https://repo.lucko.me/")
        content {
            includeModule("me.lucko", "spark-api")
        }
    }
}

val version = "1.20.6-R0.1-SNAPSHOT" // 1.20.5 has a broken dependency, but there were no changes, so it's ok

dependencies {
    implementation(projects.bukkit)
    paperweight.paperDevBundle(version)
    compileOnly("io.papermc.paper:paper-api:${version}")
}

tasks.compileJava {
    options.release.set(21)
}