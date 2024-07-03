import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("multiplatform")
    id("maven-publish")
    id("com.vanniktech.maven.publish") version "0.29.0"
}

kotlin {
    js(IR) {
        browser()
    }
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()

    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("kotlin.js.ExperimentalJsExport")
        }
    }

    targets.all {
        compilations.all {
            // Cannot enable rn due to native issue (stdlib included more than once)
            // may be related to https://youtrack.jetbrains.com/issue/KT-46636
            kotlinOptions.allWarningsAsErrors = false
        }
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
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
