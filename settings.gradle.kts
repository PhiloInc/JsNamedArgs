pluginManagement {
    val kspVersion: String by settings

    plugins {
        id("com.google.devtools.ksp") version kspVersion
    }
    repositories {
        gradlePluginPortal()
        mavenLocal()
    }
}

include(":jsnamedargs-annotations", ":jsnamedargs-compiler")
