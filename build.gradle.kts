plugins {
    id("tab.parent")
}

allprojects {
    group = "me.neznamy"
    version = "5.0.5"
    description = "An all-in-one solution that works"

    ext.set("id", "tab")
    ext.set("website", "https://github.com/NEZNAMY/TAB")
    ext.set("author", "NEZNAMY")
}

val platforms = setOf(
    projects.bukkit,
    projects.bukkit.paper,
    projects.bungeecord,
    projects.velocity,
    projects.sponge7,
    projects.sponge8,
    projects.fabric,
    projects.fabric.v1144,
    projects.fabric.v1182,
    projects.fabric.v1203,
    projects.fabric.v1213
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
