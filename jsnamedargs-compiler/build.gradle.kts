plugins {
    kotlin("jvm")
    id("maven-publish")
}

repositories {
    mavenCentral()
}

val kotlinVersion: String by project
val kspVersion: String by project
val kotlinPoetVersion: String by project

dependencies {
    implementation(project(":jsnamedargs-annotations"))
    implementation("com.squareup:kotlinpoet:$kotlinPoetVersion")
    implementation("com.squareup:kotlinpoet-ksp:$kotlinPoetVersion")
    implementation("com.google.devtools.ksp:symbol-processing:$kspVersion")
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.philo"
            artifactId = "jsnamedargs-compiler"

            from(components["java"])
        }
    }
}
