dependencies {
    implementation(projects.shared)
    compileOnly("me.lucko:spark-api:0.1-SNAPSHOT")
    compileOnly("org.kryptonmc:krypton-api:f93a12382f")
    annotationProcessor("org.kryptonmc:krypton-annotation-processor:f93a12382f")
}

tasks.compileJava {
    options.release.set(17)
}
