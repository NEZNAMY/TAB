dependencies {
    implementation(projects.shared)
    implementation("org.bstats:bstats-velocity:3.1.0")
    compileOnly("com.github.limework.redisbungee:RedisBungee-Velocity:0.11.0")
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-proxy:3.4.0-SNAPSHOT")
    compileOnlyApi("net.kyori:adventure-nbt:4.17.0")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    compileOnly("com.github.LeonMangler:PremiumVanishAPI:2.9.0-4")
    compileOnly("net.william278:velocityscoreboardapi:2.0.0")
    compileOnly("io.github.miniplaceholders:miniplaceholders-api:3.1.0")
}

tasks.compileJava {
    options.release.set(21)
}