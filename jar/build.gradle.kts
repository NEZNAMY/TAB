import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.gradleup.shadow")
}

val platforms = setOf(
    rootProject.projects.bukkit,
    rootProject.projects.bukkit.paper,
    rootProject.projects.bukkit.v18R3,
    rootProject.projects.bukkit.v112R1,
    rootProject.projects.bungeecord,
    rootProject.projects.velocity,
    rootProject.projects.sponge
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

        registerPlatform(rootProject.projects.fabric.dependencyProject, rootProject.projects.fabric.dependencyProject.tasks.named<org.gradle.jvm.tasks.Jar>("remapJar").get())

    }
    build {
        dependsOn(shadowJar)
    }
}
