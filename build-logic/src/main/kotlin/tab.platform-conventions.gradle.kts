plugins {
    id("tab.standard-conventions")
    id("com.github.johnrengelman.shadow")
}

tasks {
    shadowJar {
        archiveFileName.set("TAB-${project.name}-${project.version}.jar")
        relocate("org.bstats", "me.neznamy.tab.libs.org.bstats")
        relocate("org.json.simple", "me.neznamy.tab.libs.org.json.simple")
        relocate("net.kyori.event", "me.neznamy.tab.libs.net.kyori.event")
    }
}
