plugins {
    id("tab.parent")
}

allprojects {
    group = "me.neznamy"
    version = "5.2.6-SNAPSHOT"
    description = "An all-in-one solution that works"

    ext.set("id", "tab")
    ext.set("website", "https://github.com/NEZNAMY/TAB")
    ext.set("author", "NEZNAMY")
    ext.set("credits", "Joseph T. McQuigg (JT122406)")
}

val platforms = setOf(
    projects.bukkit,
    projects.bukkit.paper1205,
    projects.bukkit.paper1212,
    projects.bukkit.paper1214,
    projects.bukkit.v18R3,
    projects.bukkit.v112R1,
    projects.bukkit.v116R3,
    projects.bukkit.v117R1,
    projects.bukkit.v118R2,
    projects.bukkit.v119R1,
    projects.bukkit.v119R2,
    projects.bukkit.v119R3,
    projects.bukkit.v120R1,
    projects.bungeecord,
    projects.component,
    projects.velocity,
    projects.sponge,
    projects.fabric,
    projects.neoforge,
    projects.forge
).map { it.dependencyProject }

val special = setOf(
    projects.api,
    projects.shared
).map { it.dependencyProject }

subprojects {
    when (this) {
        in platforms -> plugins.apply("tab.platform-conventions")
        in special -> plugins.apply("tab.standard-conventions")
        else -> plugins.apply("tab.base-conventions")
    }
}
