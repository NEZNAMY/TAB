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
