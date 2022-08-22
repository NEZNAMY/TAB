plugins {
    id("tab.base-conventions")
    id("net.kyori.indra.publishing")
}

indra {
    publishReleasesTo("krypton-repo", "https://repo.kryptonmc.org/releases")
    publishSnapshotsTo("krypton-repo", "https://repo.kryptonmc.org/snapshots")
}
