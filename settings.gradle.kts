enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    repositories {
        mavenCentral() // Netty, SnakeYaml, json-simple, Gson, slf4j, Guava, Kyori event, bStats, AuthLib
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
        maven("https://repo.viaversion.com/") // ViaVersion
        maven("https://repo.essentialsx.net/releases/") // Essentials
        maven("https://repo.codemc.org/repository/maven-public/") // RedisBungee
        maven("https://repo.codemc.io/repository/maven-snapshots/") // Fixing compilation
        maven("https://repo.kryptonmc.org/releases") // YamlAssist, LuckPerms
        maven("https://repo.kryptonmc.org/snapshots") // BungeeCord
        maven("https://repo.opencollab.dev/maven-snapshots/") // Floodgate
        maven("https://repo.purpurmc.org/snapshots") // Purpur, Vault
        maven("https://repo.papermc.io/repository/maven-public/") // Velocity
    }
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "TAB"

include(":api")
include(":shared")
include(":krypton")
include(":velocity")
include(":bukkit")
include(":bungeecord")
include(":jar")
