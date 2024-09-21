dependencies {
    implementation(projects.shared)
    implementation("org.bstats:bstats-bukkit:3.0.1")
    compileOnly("org.purpurmc.purpur:purpur-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.mojang:authlib:1.5.25")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude("org.bukkit", "bukkit")
    }
    compileOnly("LibsDisguises:LibsDisguises:10.0.21") {
        exclude("org.spigotmc", "spigot")
        exclude("org.spigotmc", "spigot-api")
        exclude("com.github.dmulloy2", "ProtocolLib")
        exclude("org.ow2.asm", "asm")
        exclude("net.md-5", "bungeecord-chat")
    }
    compileOnly("com.github.LeonMangler:PremiumVanishAPI:2.8.8")
}
