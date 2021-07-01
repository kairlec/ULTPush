plugins {
    kotlin("jvm") apply true
}

dependencies {
    api(kotlin("stdlib"))
    api(kotlin("reflect"))
    implementation("io.javalin:javalin:${com.kairlec.ultpush.gradle.Versions.javalin}")
    api(project(":ultpush-http"))
    api(project(":ultpush-configuration"))
    implementation("org.slf4j:slf4j-simple:${com.kairlec.ultpush.gradle.Versions.slf4j}")
    testImplementation("org.slf4j:slf4j-simple:${com.kairlec.ultpush.gradle.Versions.slf4j}")
    testCompileOnly("org.junit.jupiter:junit-jupiter-api:${com.kairlec.ultpush.gradle.Versions.junit}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${com.kairlec.ultpush.gradle.Versions.junit}")
}
tasks.withType<Test> {
    useJUnitPlatform()
    workingDir = rootDir
    val files = File("${rootDir}/debug/plugins").list { _, name ->
        name.endsWith(".jar")
    }?.map { "${rootDir}/debug/plugins/${it}" } ?: ArrayList()
    classpath += files(files.toTypedArray())
}

tasks.withType<Jar> {
    from(configurations.compileClasspath.get().filter { it.exists() }.map {
        if (it.isDirectory) it else zipTree(it)
    })
}

task("copy") {
    copy {
        val jarFilePath = "${project.buildDir}/libs/${project.name}-${project.version}.jar"
        val targetPath = "${rootProject.projectDir}/debug/plugins"
        println("$jarFilePath --> $targetPath")
        from(jarFilePath)
        into(targetPath)
    }
}