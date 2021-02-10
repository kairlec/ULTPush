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