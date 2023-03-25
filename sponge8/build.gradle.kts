import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.gradle.vanilla.repository.MinecraftPlatform

plugins {
    id("org.spongepowered.gradle.plugin") version "2.0.2"
    id("org.spongepowered.gradle.vanilla") version "0.2.1-SNAPSHOT"
}

repositories {
    // Gradle doesn't support combining settings and project repositories, so we have to re-declare all the settings repos we need
    maven("https://repo.kryptonmc.org/releases")
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.viaversion.com/")

}

dependencies {
    implementation(projects.shared)
    implementation(libs.bstats.sponge)
}

minecraft {
    version("1.16.5")
    platform(MinecraftPlatform.SERVER)
}

sponge {
    apiVersion(libs.sponge8.get().version)
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    license("LICENSE")
    plugin(rootProject.ext.get("id")!!.toString()) {
        displayName(rootProject.name)
        version(project.version.toString())
        description(project.description)
        entrypoint("me.neznamy.tab.platforms.sponge8.Sponge8TAB")
        links {
            val website = rootProject.ext.get("website")!!.toString()
            homepage(website)
            source(website)
            issues("$website/issues")
        }
        contributor(rootProject.ext.get("author")!!.toString()) {}
    }
}

afterEvaluate {
    // VanillaGradle adds all the Minecraft dependencies to the runtime classpath, which we don't want.
    val runtime = configurations.runtimeClasspath.get()
    runtime.setExtendsFrom(runtime.extendsFrom.minusElement(configurations.minecraft.get()))
}
