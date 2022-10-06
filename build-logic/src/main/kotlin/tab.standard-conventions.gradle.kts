plugins {
    id("tab.base-conventions")
    id("net.kyori.indra.publishing")
}

indra {
    publishReleasesTo("krypton", "https://repo.kryptonmc.org/releases")
    publishSnapshotsTo("krypton", "https://repo.kryptonmc.org/snapshots")
    configurePublications {
        artifactId = "tab-$artifactId"
    }
}
