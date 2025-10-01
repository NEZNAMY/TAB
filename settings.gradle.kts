enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        mavenCentral() // Netty, SnakeYaml, json-simple, Guava, Kyori event, bStats, AuthLib, LuckPerms
        maven("https://repo.viaversion.com/") // ViaVersion
        maven("https://repo.william278.net/releases/") // VelocityScoreboardAPI
        maven("https://repo.codemc.org/repository/nms/") // CraftBukkit + NMS
        maven("https://repo.papermc.io/repository/maven-public/") // paperweight, Velocity, Adventure
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
        maven("https://repo.opencollab.dev/maven-snapshots/") // Floodgate, Bungeecord-proxy
        maven("https://repo.purpurmc.org/snapshots") // Purpur
        maven("https://repo.spongepowered.org/repository/maven-public/") // Sponge
        maven("https://jitpack.io") // PremiumVanish, Vault, YamlAssist, RedisBungee
        maven("https://repo.md-5.net/content/groups/public/") // LibsDisguises
    }
}

pluginManagement {
    includeBuild("build-logic")
    repositories {
        maven("https://repo.spongepowered.org/repository/maven-public/")
        maven("https://maven.architectury.dev/")
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "TAB"

include(":api")
include(":shared")
include(":velocity")
include(":bukkit")
include(":bukkit:paper_1_20_5")
include(":bukkit:paper_1_21_2")
include(":bukkit:paper_1_21_4")
include(":bukkit:paper_1_21_9")
include(":bukkit:v1_7_R1")
include(":bukkit:v1_7_R2")
include(":bukkit:v1_7_R3")
include(":bukkit:v1_7_R4")
include(":bukkit:v1_8_R1")
include(":bukkit:v1_8_R2")
include(":bukkit:v1_8_R3")
include(":bukkit:v1_9_R1")
include(":bukkit:v1_9_R2")
include(":bukkit:v1_10_R1")
include(":bukkit:v1_11_R1")
include(":bukkit:v1_12_R1")
include(":bukkit:v1_13_R1")
include(":bukkit:v1_13_R2")
include(":bukkit:v1_14_R1")
include(":bukkit:v1_15_R1")
include(":bukkit:v1_16_R1")
include(":bukkit:v1_16_R2")
include(":bukkit:v1_16_R3")
include(":bukkit:v1_17_R1")
include(":bukkit:v1_18_R1")
include(":bukkit:v1_18_R2")
include(":bukkit:v1_19_R1")
include(":bukkit:v1_19_R2")
include(":bukkit:v1_19_R3")
include(":bukkit:v1_20_R1")
include(":bukkit:v1_20_R2")
include(":bukkit:v1_20_R3")
include(":bukkit:v1_20_R4")
include(":bukkit:v1_21_R1")
include(":bukkit:v1_21_R2")
include(":bukkit:v1_21_R3")
include(":bukkit:v1_21_R4")
include(":bukkit:v1_21_R5")
include(":bukkit:v1_21_R6")
include(":bungeecord")
include(":sponge")
include(":fabric")
include(":neoforge")
include(":forge")
include(":jar")
