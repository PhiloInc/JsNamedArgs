import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("com.vanniktech.maven.publish") version "0.29.0"
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

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    if (!project.hasProperty("skipSigning")) {
        signAllPublications()
    }
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
