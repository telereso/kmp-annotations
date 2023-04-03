pluginManagement {
    val kspVersion: String by settings
    plugins {
        id("com.google.devtools.ksp") version kspVersion apply false
    }
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
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
includeBuild("gradle-plugin")
include(":annotations")
include(":processor")
include(":app")

include(":lib")
project(":lib").name = "annotations-client"
include(":models")
project(":models").name = "annotations-models"

include(":androidApp")
include(":jvmApi")