val teleresoKmpCatalog: String by settings

pluginManagement {
    plugins {
    }
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://s01.oss.sonatype.org/content/groups/staging") }
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
        maven(url = "https://pkgs.dev.azure.com/burnoo/maven/_packaging/public/maven/v1") {
            content {
                includeVersionByRegex(".*", ".*", ".*-beap[0-9]+")
            }
        }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
//        maven { url = uri("https://s01.oss.sonatype.org/content/groups/staging") }
        maven(url = "https://pkgs.dev.azure.com/burnoo/maven/_packaging/public/maven/v1") {
            content {
                includeVersionByRegex(".*", ".*", ".*-beap[0-9]+")
            }
        }
    }
    versionCatalogs {
        create("kmpLibs") {
            from("io.telereso.kmp:catalog:$teleresoKmpCatalog")
            version("teleresoCore", "0.2.0-wasm-local")
            version("teleresoKmp", "0.0.1-local")
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