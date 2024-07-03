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
    version = "0.0.6"

    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
}
