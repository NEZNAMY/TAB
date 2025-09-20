dependencies {
    implementation(projects.bukkit)
    compileOnly("org.spigotmc:spigot:1.19.4-R0.1-SNAPSHOT") {
        // Broken dependencies
        exclude("com.mojang", "authlib")
        exclude("com.mojang", "logging")
        exclude("com.mojang", "datafixerupper")
    }
    compileOnly("com.mojang:authlib:1.5.25")
}

tasks.compileJava {
    options.release.set(14) // Records
}
