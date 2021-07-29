import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") apply true
}

dependencies {
    api(kotlin("stdlib"))
    api(kotlin("reflect"))
    api(project(":ultpush-bind"))
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=com.kairlec.ultpush.http.UnofficialStatusCode"
}