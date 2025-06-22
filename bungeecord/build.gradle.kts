dependencies {
    implementation(projects.shared)
    implementation("org.bstats:bstats-bungeecord:3.1.0")
    compileOnly("net.md-5:bungeecord-api:1.21-R0.4-SNAPSHOT")
    compileOnly("net.md-5:bungeecord-proxy:1.21-R0.3-SNAPSHOT") // R0.4 is not available anywhere, combine with new API
    compileOnly("com.github.limework.redisbungee:RedisBungee-Bungee:0.11.0")
    compileOnly("com.github.LeonMangler:PremiumVanishAPI:2.8.8")
}