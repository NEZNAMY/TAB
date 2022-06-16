enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    repositories {
        mavenCentral() // Netty, SnakeYaml, json-simple, Gson, slf4j, Guava, Kyori event, bStats
        maven("https://jitpack.io") // PremiumVanish
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
        maven("https://repo.viaversion.com/") // ViaVersion
        maven("https://repo.essentialsx.net/releases/") // Essentials
        maven("https://repo.codemc.org/repository/maven-public/") // RedisBungee, BungeeCord
        maven("https://repo.kryptonmc.org/releases") // YamlAssist, LuckPerms
        maven("https://repo.opencollab.dev/maven-snapshots/") // Floodgate
        maven("https://repo.purpurmc.org/snapshots") // Purpur, LibsDisguises, AuthLib, Vault
    }
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

pluginManagement {
    includeBuild("build-logic")
}

rootProject.name = "TAB"

include(":api")
include(":shared")
//include(":krypton")
include(":velocity")
include(":bukkit")
include(":bungeecord")
include(":jar")
