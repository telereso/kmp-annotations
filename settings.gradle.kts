pluginManagement {
    val kspVersion: String by settings
    plugins {
        id("com.google.devtools.ksp") version kspVersion apply false
    }
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "kmp-annotations"
includeBuild("convention-plugins")
include("gradle-plugin")
include(":annotations")
include(":processor")
include(":app")
