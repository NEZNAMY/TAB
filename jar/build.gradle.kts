import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow")
}

val platforms = setOf(
    rootProject.projects.bukkit,
    rootProject.projects.bungeecord,
    rootProject.projects.velocity,
    rootProject.projects.sponge7,
    rootProject.projects.sponge8,
).map { it.dependencyProject }

tasks {
    shadowJar {
        archiveFileName.set("TAB-${project.version}.jar")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        fun registerPlatform(project: Project, shadeTask: org.gradle.jvm.tasks.Jar) {
            dependsOn(shadeTask)
            dependsOn(project.tasks.withType<Jar>())
            from(zipTree(shadeTask.archiveFile))
        }

        platforms.forEach {
            registerPlatform(it, it.tasks.named<ShadowJar>("shadowJar").get())
        }

        // Fabric is different
        val fabricProject = rootProject.projects.fabric.dependencyProject
        registerPlatform(fabricProject, fabricProject.tasks.named<org.gradle.jvm.tasks.Jar>("remapJar").get())
    }
    build {
        dependsOn(shadowJar)
    }
}
