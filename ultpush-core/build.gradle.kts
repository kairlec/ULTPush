import com.kairlec.ultpush.gradle.Versions

plugins {
    kotlin("jvm") apply true
    `java-library`
    `maven-publish`
}

dependencies {
    api("org.slf4j:slf4j-api:${Versions.slf4j}")
    api(kotlin("stdlib"))
    api(kotlin("reflect"))
    api("org.jetbrains.kotlinx", "kotlinx-coroutines-core", Versions.kotlinCoroutines)
    api(project(":ultpush-bind"))
    testImplementation("org.slf4j:slf4j-simple:${Versions.slf4j}")
}


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "ultpush-core"
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set("ultpush-core")
                description.set("ultpush-core")
                url.set("https://www.github.com/kairlec/ultpush")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("kairlec")
                        name.set("Kairlec")
                        email.set("sunfokairlec@gmail.com")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            val releasesRepoUrl = uri("$buildDir/repos/releases")
            val snapshotsRepoUrl = uri("$buildDir/repos/snapshots")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}