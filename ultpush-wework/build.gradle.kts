import com.kairlec.ultpush.gradle.Versions

plugins {
    kotlin("jvm") apply true
}

dependencies {
    api("org.slf4j:slf4j-api:${Versions.slf4j}")
    api(kotlin("stdlib"))
    api(kotlin("reflect"))
    api("org.jetbrains.kotlinx", "kotlinx-coroutines-core", Versions.kotlinCoroutines)
    api(project(":ultpush-core"))
    api(project(":ultpush-wework"))
    testImplementation("org.slf4j:slf4j-simple:${Versions.slf4j}")
}

