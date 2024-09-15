val teleresoKmpCatalog: String by settings

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://s01.oss.sonatype.org/content/groups/staging") }
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven { url = uri("https://s01.oss.sonatype.org/content/groups/staging") }
    }
    versionCatalogs {
        create("kmpLibs") {
            from("io.telereso.kmp:catalog:$teleresoKmpCatalog")
              version("teleresoKmp", "0.0.1-local")
//            version("kotlin", "2.0.20")
//            version("ksp", "2.0.20-1.0.24")
//            version("teleresoCore", "0.1.9")
//            version("buildConfig", "0.15.1")
        }
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