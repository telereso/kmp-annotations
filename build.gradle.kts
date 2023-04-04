import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    //trick: for the same plugin versions in all sub-modules
    id("com.android.application").version("7.3.1").apply(false)
    id("com.android.library").version("7.3.1").apply(false)

    id("org.jetbrains.kotlin.android").version("1.7.21").apply(false)
    id("org.jetbrains.kotlin.plugin.parcelize").version("1.7.21").apply(false)
    kotlin("multiplatform").version("1.7.21").apply(false)
    id("org.jetbrains.kotlin.native.cocoapods").version("1.7.22").apply(false)

    id("com.squareup.sqldelight").version("1.5.3").apply(false)
}

group = "io.telereso.kmp"
version = project.findProperty("publishVersion") ?: "0.0.1-local"


// Packaging
val groupId by extra { "io.telereso" }
val scope by extra { "telereso" }

// Android
val buildToolsVersion by extra { "31.0.0" }
val minSdkVersion by extra { 21 }
val compileSdkVer by extra { 31 }
val targetSdkVersion by extra { 31 }

// Dependencies versions
val ktorVersion by extra { "2.1.3" }
val sqlDelightVersion by extra { "1.5.4" }
val coroutinesVersion by extra { "1.6.4" }
val kotlinxDatetimeVersion by extra { "0.4.0" }
val coreVersion by extra { "0.0.17" }


allprojects {
    ext {
        set("minSdkVersions", 21)
        set("ktorVersions", "2.1.0")
    }
    configurations.all {
        resolutionStrategy {
            force("io.telereso.kmp:annotations:0.0.1-local")
        }
    }
}
//tasks.test {
//  useJUnitPlatform()
//}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

buildscript {

    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.22")
        classpath("com.android.tools.build:gradle:7.0.3")
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.4.21")
        classpath("com.squareup.sqldelight:gradle-plugin:1.5.4")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.7.20")
        classpath("org.jfrog.buildinfo:build-info-extractor-gradle:latest.release")
        classpath("com.codingfeline.buildkonfig:buildkonfig-gradle-plugin:0.13.3")
    }
}

allprojects {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}
//tasks.register("clean", Delete::class) {
//    delete(rootProject.buildDir)
//}

//task("installGitHook", Copy::class) {
//    from(File(rootProject.rootDir, "scripts/pre-push"))
//    into(File(rootProject.rootDir, ".git/hooks/"))
//    fileMode = 777
//}

gradle.projectsEvaluated {
    subprojects {
        rootProject.tasks.findByName("installGitHook")?.let {
            tasks.findByName("preBuild")?.dependsOn(it)
        }
    }
}

tasks.register("cleanAll", Delete::class) {
    delete(rootProject.buildDir)
    delete(rootDir.resolve("lib").resolve("build"))
    delete(rootDir.resolve("models").resolve("build"))
    delete(rootDir.resolve("androidApp").resolve("build"))
    delete(rootDir.resolve("webApp").resolve("build"))
    delete(rootDir.resolve("webApp").resolve("node_modules"))
    delete(rootDir.resolve("jsApi").resolve("build"))
    delete(rootDir.resolve("jsApi").resolve("node_modules"))
    delete(rootDir.resolve("iosApp").resolve("Pods"))
    val reactNativeDir = rootDir.resolve("react-native-annotations-client")
    delete(reactNativeDir.resolve("node_modules"))
    delete(reactNativeDir.resolve("lib"))
    delete(reactNativeDir.resolve("android").resolve("build"))
    val exampleDir = reactNativeDir.resolve("example")
    delete(exampleDir.resolve("node_modules"))
    delete(exampleDir.resolve("android").resolve("build"))
    delete(exampleDir.resolve("android").resolve("app").resolve("build"))
    delete(exampleDir.resolve("ios").resolve("build"))
    delete(exampleDir.resolve("ios").resolve("Pods"))
}

tasks.register("initAll") {
    doLast {
//        exec {
//            commandLine("chmod +x ./gradlew".split(" "))
//        }

        exec {
            workingDir = rootDir.resolve("iosApp")
            commandLine("pod install".split(" "))
        }

        exec {
            workingDir = rootDir.resolve("webApp")
            commandLine("yarn")
        }

        exec {
            workingDir = rootDir.resolve("jsApi")
            commandLine("yarn")
        }

        val reactNativeDr = rootDir.resolve("react-native-${project.name}")
//        exec {
//            workingDir = reactNativeDr
//            commandLine("chmod +x example/android/gradlew".split(" "))
//        }

        exec {
            workingDir = reactNativeDr
            commandLine("yarn".split(" "))
        }
        exec {
            workingDir = reactNativeDr
            commandLine("yarn bootstrap".split(" "))
        }
    }
}

tasks.register("webApp") {
    doLast {
        exec {
            workingDir = rootDir.resolve("webApp")
            commandLine("yarn serve".split(" "))
        }
    }
}

tasks.register("jsApi") {
    doFirst {
        exec {
            workingDir = rootDir.resolve("jsApi")
            commandLine("npx kill-port 3000".split(" "))
        }
    }
    doLast {
        exec {
            workingDir = rootDir.resolve("jsApi")
            commandLine("yarn rs".split(" "))
        }
    }
}


tasks.register("rnAndroidApp") {
    doLast {
        exec {
            workingDir = rootDir.resolve("react-native-${project.name}")
            commandLine("yarn example android".split(" "))
        }
    }
}

tasks.register("rnIosApp") {
    doLast {
        exec {
            workingDir = rootDir.resolve("react-native-${project.name}")
            commandLine("yarn example ios".split(" "))
        }
    }
}
