plugins {
    id("io.papermc.paperweight.userdev")
}

dependencies {
    implementation(projects.bukkit.nms.adapter)
    paperDevBundle("1.17.1-R0.1-SNAPSHOT")
}
