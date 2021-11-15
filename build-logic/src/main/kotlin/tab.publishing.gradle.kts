plugins {
    id("tab.base-conventions")
    `maven-publish`
}

publishing {
    publications.create<MavenPublication>("mavenJava") {
        groupId = rootProject.group as String
        artifactId = project.name
        version = rootProject.version as String
    }
    // TODO: Add the repository to publish to
}
