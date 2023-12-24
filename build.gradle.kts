import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(kmpLibs.plugins.kotlin.jvm) apply false
    alias(kmpLibs.plugins.kotlin.multiplatform) apply false
    alias(kmpLibs.plugins.android.library) apply false
    alias(kmpLibs.plugins.kotlin.native.cocoapods) apply false
    alias(kmpLibs.plugins.kotlin.parcelize) apply false

}

group = "io.telereso.kmp"
version = project.findProperty("publishVersion") ?: "0.0.1-local"


// Packaging
val groupId by extra { "io.telereso" }
val scope by extra { "telereso" }


allprojects {
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
    kotlinOptions.jvmTarget = "17"
}

buildscript {

    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${kmpLibs.versions.kotlin.get()}")
        classpath("com.android.tools.build:gradle:${kmpLibs.versions.agp.get()}")
    }
}

allprojects {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
        maven { url = uri("https://s01.oss.sonatype.org/content/groups/staging") }
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
