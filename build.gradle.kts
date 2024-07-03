import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost

buildscript {
    val kotlinVersion: String by project
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

plugins {
    id("maven-publish")
    id("com.vanniktech.maven.publish") version "0.29.0"
}

allprojects {
    group = "com.philo"
    version = "0.0.2-SNAPSHOT"

    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
}


subprojects {
    plugins.withId("com.vanniktech.maven.publish.base") {
        configure<MavenPublishBaseExtension> {
            publishToMavenCentral(SonatypeHost.DEFAULT, automaticRelease = true)
            signAllPublications()
            pom {
                name.set(project.name)
                description.set("Philo's KMP plugin to generate JS style functions and classes")
                url.set("https://github.com/PhiloInc/JsNamedArgs")
                inceptionYear.set("2024")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/PhiloInc/JsNamedArgs/blob/main/LICENSE.txt")
                        distribution.set("repo")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/PhiloInc/JsNamedArgs.git")
                    developerConnection.set("scm:git:ssh://git@github.com:PhiloInc/JsNamedArgs.git")
                    url.set("https://github.com/PhiloInc/JsNamedArgs")
                }
                developers {
                    developer {
                        name.set("Philo, Inc.")
                    }
                }
            }
        }
    }
}
