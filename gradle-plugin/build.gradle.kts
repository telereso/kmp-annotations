import java.util.*

plugins {
    `kotlin-dsl` // Is needed to turn our build logic written in Kotlin into Gralde Plugin
    `java-gradle-plugin`
    id("com.google.devtools.ksp") version "1.8.21-1.0.11"
    id("com.gradle.plugin-publish") version "1.0.0"
    signing
}


repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
}

// Grabbing secrets from local.properties file or from environment variables, which could be used on CI
val secretPropsFile = project.rootProject.file("local.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply {
            load(it)
        }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
    ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
    ext["signing.secretKeyRingFile"] = System.getenv("SIGNING_SECRET_KEY_RING_FILE")
    ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
    ext["ossrhPassword"] = System.getenv("OSSRH_PASSWORD")
}

group = rootProject.group
version = rootProject.version

gradlePlugin {
    plugins {
        create("kmp") {
            id = "io.telereso.kmp"
            displayName = "Kotlin multiplatform plugin"
            description =
                "Include tasks needed while working with Telereso's Kotlin multiplatform annotations also to support react native and flutter"
            implementationClass = "io.telereso.kmp.KmpPlugin"
        }
    }
}

pluginBundle {
    description =
        "Include tasks needed while working with Telereso's Kotlin multiplatform annotations also to support react native and flutter"
    website = "https://telereso.io/"
    vcsUrl = "https://github.com/telereso/kmp-annotations"
    tags = listOf("kotlin", "Kotlin Multiplatform", "kmm", "kmp", "Telereso", "ReactNative")
}

publishing {
    repositories {
        maven {
            name = "localPluginRepository"
            url = uri("${project.rootProject.buildDir}/.m2/repository")
        }
    }
}


dependencies {
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:1.8.21-1.0.11")
}