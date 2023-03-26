import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    `java-library`
    id("io.freefair.lombok")
}

tasks {
    processResources {
        filter<ReplaceTokens>("tokens" to mapOf(
            "name" to rootProject.name,
            "version" to project.version,
            "description" to project.description,
            "website" to rootProject.ext.get("website")!!.toString(),
            "author" to rootProject.ext.get("author")!!.toString()
        ))
    }
    javadoc {
        // This saves a decent bit of processing power on slow machines
        enabled = false
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
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
