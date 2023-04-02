plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.plugin.shadow)
    implementation(libs.plugin.lombok)
    implementation(libs.plugin.loom)
}

dependencies {
    compileOnly(files(libs::class.java.protectionDomain.codeSource.location))
}
