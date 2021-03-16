import com.kairlec.ultpush.gradle.Versions

plugins {
    kotlin("jvm") apply true
}

dependencies {
    api(kotlin("stdlib"))
    api(kotlin("reflect"))
    api(project(":ultpush-bind"))
    api("org.jetbrains.kotlinx", "kotlinx-coroutines-core", Versions.kotlinCoroutines)
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