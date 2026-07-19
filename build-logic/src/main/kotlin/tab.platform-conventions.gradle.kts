plugins {
    id("tab.standard-conventions")
    id("com.gradleup.shadow")
}

tasks.withType<Zip>().configureEach {
    isZip64 = true
}

tasks {
    shadowJar {
        archiveFileName.set("TAB-${project.name}-${project.version}.jar")
        relocate("org.bstats", "me.neznamy.tab.libs.org.bstats")
        relocate("org.json.simple", "me.neznamy.tab.libs.org.json.simple")
        relocate("net.kyori.event", "me.neznamy.tab.libs.net.kyori.event")
        relocate("me.neznamy.yamlassist", "me.neznamy.tab.libs.me.neznamy.yamlassist")
        relocate("org.yaml.snakeyaml", "me.neznamy.tab.libs.org.yaml.snakeyaml")
        relocate("redis.clients.jedis", "me.neznamy.tab.libs.redis.clients.jedis")
        relocate("org.apache.commons.pool2", "me.neznamy.tab.libs.org.apache.commons.pool2")
        relocate("org.json", "me.neznamy.tab.libs.org.json")
        relocate("com.rabbitmq", "me.neznamy.tab.libs.com.rabbitmq")
        relocate("com.saicone.delivery4j", "me.neznamy.tab.libs.com.saicone.delivery4j")
    }
}
