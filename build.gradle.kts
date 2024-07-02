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
}

allprojects {
    group = "com.philo"
    version = "0.0.0"

    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
}
