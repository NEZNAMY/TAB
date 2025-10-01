import org.spongepowered.gradle.plugin.config.PluginLoaders

plugins {
    id("org.spongepowered.gradle.plugin") version "2.3.0"
}

repositories {
    // Gradle doesn't support combining settings and project repositories, so we have to re-declare all the settings repos we need
    mavenCentral() // Couldn't compile Caffeine 2.9.2 from jitpack
    maven("https://jitpack.io") // YamlAssist
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.viaversion.com/")
    maven("https://repo.papermc.io/repository/maven-public/") // Adventure
}

dependencies {
    implementation(projects.shared)
    implementation("org.bstats:bstats-sponge:3.1.0")
}

tasks.compileJava {
    options.release.set(21)
}

sponge {
    apiVersion("12.0.0")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    license("LICENSE")
    plugin(rootProject.ext.get("id")!!.toString()) {
        displayName(rootProject.name)
        version(project.version.toString())
        description(project.description)
        entrypoint("me.neznamy.tab.platforms.sponge.SpongeTAB")
        links {
            val website = rootProject.ext.get("website")!!.toString()
            homepage(website)
            source(website)
            issues("$website/issues")
        }
        contributor(rootProject.ext.get("author")!!.toString()) {}
    }
}
