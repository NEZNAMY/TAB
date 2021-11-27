dependencies {
    api(projects.shared)
    implementation(libs.bstats.bukkit)
    compileOnlyApi(libs.bukkit)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.vault)
    compileOnly(libs.via)
    compileOnly(libs.authlib)
    compileOnly(libs.libsDisguises)
    compileOnly(libs.essentials)

    // NMS
    implementation(projects.bukkit.nms.adapter)
}
