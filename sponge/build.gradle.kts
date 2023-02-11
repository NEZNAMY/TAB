import org.spongepowered.gradle.vanilla.repository.MinecraftPlatform

plugins {
    id("org.spongepowered.gradle.plugin") version "2.0.2"
    id("org.spongepowered.gradle.vanilla") version "0.2.1-SNAPSHOT"
}

dependencies {
    implementation(projects.shared)
    compileOnly(libs.sponge)
}

minecraft {
    version("1.16.5")
    platform(MinecraftPlatform.SERVER)
}
