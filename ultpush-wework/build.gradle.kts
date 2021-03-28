import com.kairlec.ultpush.gradle.Versions

plugins {
    kotlin("jvm") apply true
}

dependencies {
    api(kotlin("stdlib"))
    api(kotlin("reflect"))
    api(project(":ultpush-core"))
    api(project(":ultpush-http"))
    api(project(":ultpush-configuration"))
    api(project(":ultpush-user"))
    api("org.slf4j:slf4j-api:${Versions.slf4j}")
    api("org.jetbrains.kotlinx", "kotlinx-coroutines-core", Versions.kotlinCoroutines)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinxSerialization}")
    implementation("org.jetbrains.exposed:exposed-core:${Versions.exposed}")
    implementation("org.jetbrains.exposed:exposed-dao:${Versions.exposed}")
    implementation("org.jetbrains.exposed:exposed-jdbc:${Versions.exposed}")
    implementation("org.xerial:sqlite-jdbc:${Versions.sqlite}")
    implementation("io.ktor:ktor-client-core:${Versions.ktor}")
    implementation("io.ktor:ktor-client-cio:${Versions.ktor}")
    implementation("io.ktor:ktor-client-serialization:${Versions.ktor}")
    testCompileOnly("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
    testImplementation("org.slf4j:slf4j-simple:${Versions.slf4j}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
    testImplementation("org.reflections:reflections:${Versions.reflections}")
}

tasks.withType<Test> {
    useJUnitPlatform()
    workingDir = rootDir
    val files = File("${rootDir}/debug/plugins").list { _, name ->
        name.endsWith(".jar")
    }?.map { "${rootDir}/debug/plugins/${it}" } ?: ArrayList()
    classpath += files(files.toTypedArray())
}
//tasks.withType<Jar> {
//    from(configurations.compileClasspath.get().filter { it.exists() }.map {
//        println(if (it.isDirectory) it else zipTree(it))
//        if (it.isDirectory) it else zipTree(it)
//    })
//}
//
//task("copy") {
//    copy {
//        val jarFilePath = "${project.buildDir}/libs/${project.name}-${project.version}.jar"
//        val targetPath = "${rootProject.projectDir}/debug/plugins"
//        println("$jarFilePath --> $targetPath")
//        from(jarFilePath)
//        into(targetPath)
//    }
//}