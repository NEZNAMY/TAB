dependencies {
    implementation(projects.bukkit)
    compileOnly("org.spigotmc:spigot:1.20.3-R0.1-SNAPSHOT")
}

tasks.compileJava {
    options.release.set(17) // New Authlib requires Java 17+
}
