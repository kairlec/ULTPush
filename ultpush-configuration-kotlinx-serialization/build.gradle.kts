import com.kairlec.ultpush.gradle.Versions

plugins {
    kotlin("jvm") apply true
}

group = "com.kairlec"
version = "2.0-dev1"


dependencies {
    api(kotlin("stdlib"))
    api(kotlin("reflect"))
    api(project(":ultpush-configuration"))
    api(project(":ultpush-bind"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinxSerialization}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-properties:${Versions.kotlinxSerialization}")
}

tasks.withType<Jar> {
    exclude("META-INF/LICENSE")
    exclude("META-INF/NOTICE")
    exclude("**/module-info.class")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
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