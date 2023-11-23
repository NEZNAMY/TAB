enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        mavenCentral() // Netty, SnakeYaml, json-simple, Guava, Kyori event, bStats, AuthLib, LuckPerms
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
        maven("https://repo.viaversion.com/") // ViaVersion
        maven("https://repo.opencollab.dev/maven-snapshots/") // Floodgate
        maven("https://repo.purpurmc.org/snapshots") // Purpur
        maven("https://repo.spongepowered.org/repository/maven-public/") // Sponge
        maven("https://jitpack.io") // PremiumVanish, Vault, YamlAssist, RedisBungee
        maven("https://repo.md-5.net/content/groups/public/") // LibsDisguises
        maven("https://nexus.codecrafter47.dyndns.eu/content/repositories/public/") // BungeeCord-proxy // I feel bad for doing this
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
include(":fabric:v1_14_4")
include(":fabric:v1_15_2")
include(":fabric:v1_16_5")
include(":fabric:v1_17")
include(":fabric:v1_17_1")
include(":fabric:v1_18_2")
include(":fabric:v1_19_2")
include(":fabric:v1_19_3")
include(":fabric:v1_20_1")
include(":fabric:v1_20_2")
include(":fabric:v1_20_3")
include(":jar")