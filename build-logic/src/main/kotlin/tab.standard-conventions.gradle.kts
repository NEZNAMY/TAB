import gradle.kotlin.dsl.accessors._4cbb0020bd75d48acc42cabded5fdbb7.publishing
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.repositories

plugins {
    id("tab.base-conventions")
    `maven-publish`
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    repositories {
        maven {
            name = "krypton"
            url = uri("https://repo.kryptonmc.org/releases")
            credentials(PasswordCredentials::class)
        }
    }
    publications.create<MavenPublication>("mavenJava") {
        groupId = rootProject.group as String
        artifactId = "tab-${project.name}"
        version = rootProject.version as String
        from(components["java"])
    }
}
