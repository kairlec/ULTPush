plugins {
    kotlin("jvm") apply true
}

dependencies {
    api(kotlin("stdlib"))
    api(kotlin("reflect"))
}
repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public/")
    maven("https://maven.aliyun.com/repository/google/")
    maven("https://maven.aliyun.com/repository/jcenter/")
    maven("https://maven.aliyun.com/repository/gradle-plugin/")
    mavenCentral()
    jcenter()
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
//        val targetPath = "${rootProject.projectDir}/plugins"
//        println("$jarFilePath --> $targetPath")
//        from(jarFilePath)
//        into(targetPath)
//    }
//}