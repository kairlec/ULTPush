import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version com.kairlec.ultpush.gradle.Versions.kotlin
    kotlin("plugin.serialization") version com.kairlec.ultpush.gradle.Versions.kotlin
    `java-library`
    `maven-publish`
    signing
}

java {
    withJavadocJar()
    withSourcesJar()
}

group = "com.kairlec"
version = "2.0-dev1"


subprojects {

    version = "2.0-dev1"

    apply {
        plugin("kotlin")
        plugin("signing")
        plugin("maven-publish")
        plugin("java-library")
        plugin("kotlinx-serialization")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    dependencies {
        gradleApi()
    }

    tasks.javadoc {
        if (JavaVersion.current().isJava9Compatible) {
            (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
        }
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

tasks.forEach {
    it.enabled = false
}

tasks.withType<Delete> {
    File("${rootDir}/debug/plugins").list { _, name ->
        name.endsWith(".jar")
    }?.forEach { File("${rootDir}/debug/plugins/${it}").delete() }
}

buildscript {
    dependencies {
        classpath(kotlin("serialization", version = com.kairlec.ultpush.gradle.Versions.kotlin))
    }
}