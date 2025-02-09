plugins {
    id("fabric-loom")
}

dependencies {
    api(projects.bossbar.bossbarShared)
    minecraft("com.mojang:minecraft:25w06a")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.15.9")
}

tasks {
    compileJava {
        options.release.set(17)
    }
}
