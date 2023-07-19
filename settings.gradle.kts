enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        mavenCentral() // Netty, SnakeYaml, json-simple, slf4j, Guava, Kyori event, bStats, AuthLib
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
        maven("https://repo.viaversion.com/") // ViaVersion
        maven("https://repo.codemc.org/repository/maven-public/") // RedisBungee
        maven("https://repo.kryptonmc.org/releases") // Krypton, YamlAssist, LuckPerms
        maven("https://repo.kryptonmc.org/snapshots") // BungeeCord
        maven("https://repo.opencollab.dev/maven-snapshots/") // Floodgate
        maven("https://repo.purpurmc.org/snapshots") // Purpur
        maven("https://repo.papermc.io/repository/maven-public/") // Velocity
        maven("https://repo.spongepowered.org/repository/maven-public/") // Sponge
        maven("https://oss.sonatype.org/content/repositories/snapshots/") // Spark
        maven("https://maven.fabricmc.net/") // Fabric
        maven("https://jitpack.io") // PremiumVanish, Vault
    }
}

pluginManagement {
    includeBuild("build-logic")
    repositories {
        maven("https://repo.spongepowered.org/repository/maven-public/")
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "TAB"

include(":api")
include(":shared")
include(":velocity")
include(":bukkit")
include(":bungeecord")
include(":sponge7")
include(":sponge8")
include(":fabric")
include(":jar")
