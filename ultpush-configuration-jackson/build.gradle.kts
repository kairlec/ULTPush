import com.kairlec.ultpush.gradle.Versions

plugins {
    kotlin("jvm") apply true
}

dependencies {
    api(kotlin("stdlib"))
    api(kotlin("reflect"))
    api(project(":ultpush-configuration"))
    api(project(":ultpush-bind"))
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${Versions.jackson}")
}

tasks.withType<Jar> {
    from(configurations.compileClasspath.get().filter { it.exists() }.map {
        println(if (it.isDirectory) it else zipTree(it))
        if (it.isDirectory) it else zipTree(it)
    })
}

task("copy") {
    copy {
        val jarFilePath = "${project.buildDir}/libs/${project.name}-${project.version}.jar"
        val targetPath = "${rootProject.projectDir}/plugins"
        println("$jarFilePath --> $targetPath")
        from(jarFilePath)
        into(targetPath)
    }
}