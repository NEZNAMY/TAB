import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.jvm.tasks.Jar

plugins {
    id("com.gradleup.shadow")
}

val platformPaths = setOf(
    ":bukkit",
    ":bukkit:paper_1_20_5",
    ":bukkit:paper_1_21_2",
    ":bukkit:paper_1_21_4",
    ":bukkit:paper_1_21_9",
    ":bukkit:v1_7_R1",
    ":bukkit:v1_7_R2",
    ":bukkit:v1_7_R3",
    ":bukkit:v1_7_R4",
    ":bukkit:v1_8_R1",
    ":bukkit:v1_8_R2",
    ":bukkit:v1_8_R3",
    ":bukkit:v1_9_R1",
    ":bukkit:v1_9_R2",
    ":bukkit:v1_10_R1",
    ":bukkit:v1_11_R1",
    ":bukkit:v1_12_R1",
    ":bukkit:v1_13_R1",
    ":bukkit:v1_13_R2",
    ":bukkit:v1_14_R1",
    ":bukkit:v1_15_R1",
    ":bukkit:v1_16_R1",
    ":bukkit:v1_16_R2",
    ":bukkit:v1_16_R3",
    ":bukkit:v1_17_R1",
    ":bukkit:v1_18_R1",
    ":bukkit:v1_18_R2",
    ":bukkit:v1_19_R1",
    ":bukkit:v1_19_R2",
    ":bukkit:v1_10_R1",
    ":bukkit:v1_19_R3",
    ":bukkit:v1_20_R1",
    ":bukkit:v1_20_R2",
    ":bukkit:v1_20_R3",
    ":bukkit:v1_20_R4",
    ":bukkit:v1_21_R1",
    ":bukkit:v1_21_R2",
    ":bukkit:v1_21_R3",
    ":bukkit:v1_21_R4",
    ":bukkit:v1_21_R5",
    ":bukkit:v1_21_R6",
    ":bungeecord",
    ":velocity",
    ":sponge"
)

val moddedPaths = setOf(
    ":fabric",
    ":neoforge",
    ":forge"
)

val platforms: List<Project> = platformPaths.map { rootProject.project(it) }
val moddedPlatforms: List<Project> = moddedPaths.map { rootProject.project(it) }

tasks {
    shadowJar {
        archiveFileName.set("TAB-${project.version}.jar")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        fun registerPlatform(project: Project, jarTask: AbstractArchiveTask) {
            dependsOn(jarTask)
            dependsOn(project.tasks.withType<Jar>())
            from(zipTree(jarTask.archiveFile))
        }

        platforms.forEach { p ->
            val task = p.tasks.named<ShadowJar>("shadowJar").get()
            registerPlatform(p, task)
        }

        moddedPlatforms.forEach { p ->
            val task = p.tasks.named<Jar>("remapJar").get()
            registerPlatform(p, task)
        }
    }
    build.get().dependsOn(shadowJar)
}
