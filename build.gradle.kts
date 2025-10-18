plugins {
    id("tab.parent")
}

allprojects {
    group = "me.neznamy"
    version = "5.3.3-SNAPSHOT"
    description = "An all-in-one solution that works"

    ext.set("id", "tab")
    ext.set("website", "https://github.com/NEZNAMY/TAB")
    ext.set("author", "NEZNAMY")
    ext.set("credits", "Joseph T. McQuigg (JT122406)")
}

val platformPaths = setOf(
    ":bukkit",
    ":bukkit:paper_1_20_5",
    ":bukkit:paper_1_21_2",
    ":bukkit:paper_1_21_4",
    ":bukkit:paper_1_21_9",
    ":bukkit:v1_7_R1",
    ":bukkit:v1_7_R2",
    ":bukkit:v1_7_R3",
    ":bukkit:v1_7_R4",
    ":bukkit:v1_8_R1",
    ":bukkit:v1_8_R2",
    ":bukkit:v1_8_R3",
    ":bukkit:v1_9_R1",
    ":bukkit:v1_9_R2",
    ":bukkit:v1_10_R1",
    ":bukkit:v1_11_R1",
    ":bukkit:v1_12_R1",
    ":bukkit:v1_13_R1",
    ":bukkit:v1_13_R2",
    ":bukkit:v1_14_R1",
    ":bukkit:v1_15_R1",
    ":bukkit:v1_16_R1",
    ":bukkit:v1_16_R2",
    ":bukkit:v1_16_R3",
    ":bukkit:v1_17_R1",
    ":bukkit:v1_18_R1",
    ":bukkit:v1_18_R2",
    ":bukkit:v1_19_R1",
    ":bukkit:v1_19_R2",
    ":bukkit:v1_19_R3",
    ":bukkit:v1_20_R1",
    ":bukkit:v1_20_R2",
    ":bukkit:v1_20_R3",
    ":bukkit:v1_20_R4",
    ":bukkit:v1_21_R1",
    ":bukkit:v1_21_R2",
    ":bukkit:v1_21_R3",
    ":bukkit:v1_21_R4",
    ":bukkit:v1_21_R5",
    ":bukkit:v1_21_R6",
    ":bungeecord",
    ":velocity",
    ":sponge",
    ":fabric",
    ":neoforge",
    ":forge"
)

val specialPaths = setOf(
    ":api",
    ":shared"
)

subprojects {
    when (path) {
        in platformPaths -> plugins.apply("tab.platform-conventions")
        in specialPaths -> plugins.apply("tab.standard-conventions")
        else -> plugins.apply("tab.base-conventions")
    }
}
