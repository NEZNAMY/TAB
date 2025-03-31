plugins {
    id("tab.parent")
}

allprojects {
    group = "me.neznamy"
    version = "5.2.1-SNAPSHOT"
    description = "An all-in-one solution that works"

    ext.set("id", "tab")
    ext.set("website", "https://github.com/NEZNAMY/TAB")
    ext.set("author", "NEZNAMY")
    ext.set("credits", "Joseph T. McQuigg (JT122406)")
}

val platforms = setOf(
    projects.bukkit,
    projects.bukkit.paper,
    projects.bukkit.v18R3,
    projects.bukkit.v112R1,
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
