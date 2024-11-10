dependencies {
    implementation(projects.shared)
    implementation("org.bstats:bstats-velocity:3.1.0")
    compileOnly("com.github.limework.redisbungee:RedisBungee-Velocity:0.11.0")
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    compileOnly("com.github.LeonMangler:PremiumVanishAPI:2.9.0-4")
    compileOnly("net.william278:velocityscoreboardapi:1.0.1")
}

tasks.compileJava {
    options.release.set(17)
}