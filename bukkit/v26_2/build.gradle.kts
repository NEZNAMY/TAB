dependencies {
    implementation(projects.bukkit)
    compileOnly("org.spigotmc:spigot:26.2-R0.1-SNAPSHOT")
}

tasks.compileJava {
    options.release.set(21) // Included library "jtracy" requires Java 21
}
