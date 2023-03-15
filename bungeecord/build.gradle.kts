dependencies {
    implementation(projects.shared)
    implementation(libs.bstats.bungeecord)
    compileOnly(libs.bungeecord)
    compileOnly(libs.redisBungee)
    compileOnly("io.netty:netty-transport-native-epoll:4.1.84.Final") // Fixing compilation with 1.19.4 update
}
