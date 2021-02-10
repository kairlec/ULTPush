import com.kairlec.ultpush.gradle.Versions

plugins {
    kotlin("jvm") apply true
}

dependencies {
    api(kotlin("stdlib"))
    api(kotlin("reflect"))
    api("org.slf4j:slf4j-api:${Versions.slf4j}")
    api("com.google.inject", "guice", Versions.guice)
    api("org.jetbrains.kotlinx", "kotlinx-coroutines-core", Versions.kotlinCoroutines)
    implementation("org.reflections:reflections:${Versions.reflections}")
}
