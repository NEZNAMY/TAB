plugins {
    id("tab.parent")
}

allprojects {
    group = "me.neznamy"
    version = "4.1.1-SNAPSHOT"
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
    projects.fabric,
    projects.fabric.v1144,
    projects.fabric.v1152,
    projects.fabric.v1165,
    projects.fabric.v117,
    projects.fabric.v1171,
    projects.fabric.v1182,
    projects.fabric.v1192,
    projects.fabric.v1193,
    projects.fabric.v1201,
    projects.fabric.v1202,
    projects.fabric.v1204
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
