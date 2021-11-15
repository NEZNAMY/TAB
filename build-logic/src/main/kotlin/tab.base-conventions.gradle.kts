import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("org.sonarqube")
    `java-library`
}

tasks {
    processResources {
        filter<ReplaceTokens>("tokens" to mapOf(
            "name" to rootProject.name,
            "version" to project.version,
            "description" to project.description
        ))
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(8)
        options.compilerArgs.addAll(listOf("-nowarn", "-Xlint:-unchecked", "-Xlint:-deprecation"))
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "NEZNAMY_TAB")
        property("sonar.moduleKey", project.name)
        property("sonar.organization", "neznamy")
    }
}
