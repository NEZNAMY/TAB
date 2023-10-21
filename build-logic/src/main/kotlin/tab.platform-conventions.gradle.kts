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
        relocate("me.neznamy.yamlassist", "me.neznamy.tab.libs.me.neznamy.yamlassist")
        relocate("org.yaml.snakeyaml", "me.neznamy.tab.libs.org.yaml.snakeyaml")
    }
}
