import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("net.kyori.indra")
}

indra {
    javaVersions {
        target(8)
    }
    github("NEZNAMY", "TAB") {
        ci(true)
    }
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
        // This saves a decent bit of processing power on slow machines
        enabled = false
        options.encoding = Charsets.UTF_8.name()
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }
}
