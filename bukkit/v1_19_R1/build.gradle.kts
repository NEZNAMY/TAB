dependencies {
    implementation(projects.bukkit)
    compileOnly("org.spigotmc:spigot:1.19.2-R0.1-SNAPSHOT") {
        // Broken dependencies
        exclude("com.mojang", "authlib")
        exclude("com.mojang", "logging")
        exclude("com.mojang", "datafixerupper")
    }
    compileOnly("com.mojang:authlib:1.5.25")
}
