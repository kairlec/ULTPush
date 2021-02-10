plugins {
    kotlin("jvm")
    kotlin("plugin.spring") version com.kairlec.ultpush.gradle.Versions.kotlin apply true
    id("org.springframework.boot") version com.kairlec.ultpush.gradle.Versions.springboot
    id("io.spring.dependency-management") version com.kairlec.ultpush.gradle.Versions.springDependencyManagement
}

group = "com.kairlec"
version = "0.1-SNAPSHOT"

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public/")
    maven("https://maven.aliyun.com/repository/google/")
    maven("https://maven.aliyun.com/repository/jcenter/")
    maven("https://maven.aliyun.com/repository/gradle-plugin/")
    mavenCentral()
    jcenter()
}

dependencies {
    api(project(":ultpush-core"))
    api(kotlin("stdlib"))
    api(kotlin("reflect"))
    api("org.jetbrains.kotlinx", "kotlinx-coroutines-core", com.kairlec.ultpush.gradle.Versions.kotlinCoroutines)

    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.xerial:sqlite-jdbc:${com.kairlec.ultpush.gradle.Versions.sqlite}")
    implementation("org.reflections:reflections:${com.kairlec.ultpush.gradle.Versions.reflections}")
    implementation("org.jetbrains.exposed:exposed-spring-boot-starter:${com.kairlec.ultpush.gradle.Versions.exposed}")
    implementation("org.jetbrains.exposed:spring-transaction:${com.kairlec.ultpush.gradle.Versions.exposed}")
    implementation("org.springframework.data:spring-data-relational:${com.kairlec.ultpush.gradle.Versions.springDataRelational}")
    implementation("org.liquibase:liquibase-core:${com.kairlec.ultpush.gradle.Versions.liquibase}")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}