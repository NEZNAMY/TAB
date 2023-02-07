plugins {
    id("tab.parent")
}

allprojects {
    group = "me.neznamy"
    version = property("projectVersion") as String // from gradle.properties
    description = "An all-in-one solution that works"
}

val platforms = setOf(
    projects.bukkit,
    projects.bungeecord,
    projects.krypton,
    projects.velocity
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
