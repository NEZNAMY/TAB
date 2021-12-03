import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow")
}

val platforms = setOf(
    rootProject.projects.bukkit,
    rootProject.projects.bungeecord,
    rootProject.projects.krypton,
    rootProject.projects.velocity
).map { it.dependencyProject }

tasks {
    shadowJar {
        archiveFileName.set("TAB-${project.version}.jar")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        platforms.forEach {
            val shadowJarTask = it.tasks.named<ShadowJar>("shadowJar").forUseAtConfigurationTime().get()
            dependsOn(shadowJarTask)
            dependsOn(it.tasks.withType<Jar>())
            from(zipTree(shadowJarTask.archiveFile))
        }
    }
    build {
        dependsOn(shadowJar)
    }
}
