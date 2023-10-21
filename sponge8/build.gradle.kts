import org.spongepowered.gradle.plugin.config.PluginLoaders

plugins {
    id("org.spongepowered.gradle.plugin") version "2.0.2"
}

repositories {
    // Gradle doesn't support combining settings and project repositories, so we have to re-declare all the settings repos we need
    maven("https://jitpack.io") // YamlAssist
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.viaversion.com/")
}

dependencies {
    implementation(projects.shared)
    implementation("org.bstats:bstats-sponge:3.0.1")
}

sponge {
    apiVersion("8.0.0")
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
