plugins {
    id("tab.parent")
}

allprojects {
    group = "me.neznamy"
    version = "4.0.8-SNAPSHOT"
    description = "An all-in-one solution that works"

    ext.set("id", "tab")
    ext.set("website", "https://github.com/NEZNAMY/TAB")
    ext.set("author", "NEZNAMY")
}

val platforms = setOf(
    projects.bukkit,
    projects.bungeecord,
    projects.velocity,
    projects.sponge7,
    projects.sponge8,
    projects.fabric
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
