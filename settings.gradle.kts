pluginManagement {
    plugins {
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
include(":annotations")
include(":processor")
include(":app")

val publishGradlePlugin: String by settings

if (publishGradlePlugin.toBoolean()) {
    include("gradle-plugin")
} else {
    includeBuild("gradle-plugin")
    include(":lib")
    project(":lib").name = "annotations-client"

    include(":models")
    project(":models").name = "annotations-models"

    include(":androidApp")
    include(":jvmApi")
}