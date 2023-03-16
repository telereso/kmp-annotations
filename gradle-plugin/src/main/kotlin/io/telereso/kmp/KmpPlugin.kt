/*
 * MIT License
 *
 * Copyright (c) 2023 Telereso
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.telereso.kmp

import com.google.devtools.ksp.gradle.KspExtension
import com.google.devtools.ksp.gradle.KspGradleSubplugin
import io.telereso.kmp.TeleresoKmpExtension.Companion.teleresoKmp
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.extra

class KmpPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {

        pluginManager.apply(KspGradleSubplugin::class.java)

        val kspExtension = extensions.getByType(KspExtension::class.java)
        val scope = getScope()?.let { scope ->
            kspExtension.arg("scope", scope)
            scope
        }

        val annotationsVersion = "0.0.15"
        dependencies.add("commonMainImplementation", "io.telereso.kmp:annotations:$annotationsVersion")
        dependencies.add("kspCommonMainMetadata", "io.telereso.kmp:processor:$annotationsVersion")
//        dependencies.add("commonMainImplementation", project(":annotations"))
//        dependencies.add("kspCommonMainMetadata", project(":processor"))

        val teleresoKmp = teleresoKmp()

        afterEvaluate {
            // Common tasks

            val jsCleanLibraryDistributionTask = "jsCleanLibraryDistribution"
            tasks.create<Delete>(jsCleanLibraryDistributionTask) {
                group = "Clean"
                delete(buildDir.resolve("productionLibrary"))
            }

            tasks.named("compileKotlinJs").configure {
                dependsOn(jsCleanLibraryDistributionTask)
            }

            val dependsOnTasks = listOf(
                "compileKotlinJs",
                "compileKotlinJvm",
                "compileCommonMainKotlinMetadata",
                "compileDebugKotlinAndroid",
                "compileKotlinIosArm64",
                "compileKotlinIosSimulatorArm64",
                "compileKotlinIosX64",
                "jsBrowserProductionLibraryDistribution",
                "preBuild",
                "build",
                "allTests"
            )

            // Models tasks

//            val copyGeneratedModelsTask = "kspCommonMainKotlinMetadataCopyGeneratedModels"
//            tasks.create<Copy>(copyGeneratedModelsTask) {
//                log("Copying ksp generated models")
//                group = "Ksp"
//                from("build/generated/ksp/metadata/commonMain/resources/kotlin/")
//                into("src/commonMain/kotlin/")
//            }
//
//
//            val cleanModelsGeneratedFilesTask = "kspCommonMainKotlinClean"
//
//            tasks.create<Delete>(cleanModelsGeneratedFilesTask) {
//                group = "Clean"
//                delete(fileTree("src").matching {
//                    include("**/*.g.kt")
//                })
//            }

            if (teleresoKmp.disableJsonConverters) {
                log("Skipping adding models tasks")
            } else {
                log("Adding Models Tasks")

//                tasks.findByName("clean")?.dependsOn(cleanModelsGeneratedFilesTask)

//                tasks.getByName("kspCommonMainKotlinMetadata")
//                    .dependsOn(cleanModelsGeneratedFilesTask)

//                tasks.getByName("kspCommonMainKotlinMetadata")
//                    .finalizedBy(copyGeneratedModelsTask)

//                tasks.getByName(copyGeneratedModelsTask)
//                    .dependsOn("kspCommonMainKotlinMetadata")

//                dependsOnTasks.forEach{
//                    tasks.findByName(it)?.dependsOn(copyGeneratedModelsTask)
//                }
            }


            val projectPackageName = getProjectName()
            val baseDir = "$rootDir".split("/react-native")[0]

            log("Creating Lib Tasks for project $projectPackageName ${scope?.let { "with scope $it" }}")

            // Android
            val copyGeneratedFilesAndroidTask =
                "kspCommonMainKotlinMetadataCopyGeneratedAndroid"

            val cleanAndroidGeneratedFiles = "cleanAndroidGeneratedFiles"
            tasks.create<Delete>(cleanAndroidGeneratedFiles) {
                group = "Clean"
                delete(buildDir.resolve("generated/ksp/metadata/commonMain/kotlin"))
            }

            tasks.create<Copy>(copyGeneratedFilesAndroidTask) {
                log("Copying ksp generated reactNative android files")

                from("${buildDir.path}/generated/ksp/metadata/commonMain/kotlin/")
                into(
                    "${baseDir}/react-native-${
                        projectPackageName.replace(
                            ".",
                            "-"
                        )
                    }/android/src/main/java/"
                )

            }

            // iOS
            val copyGeneratedFilesIosTask = "kspCommonMainKotlinMetadataCopyGeneratedIos"

            tasks.create<Copy>(copyGeneratedFilesIosTask) {
                log("Copying ksp generated reactNative ios files")

                from("${buildDir.path}/generated/ksp/metadata/commonMain/resources/ios")
                into(
                    "${baseDir}/react-native-${
                        projectPackageName.replace(
                            ".",
                            "-"
                        )
                    }/ios/"
                )
            }

            // Js
            val copyGeneratedFilesJsTask = "kspCommonMainKotlinMetadataCopyGeneratedJs"

            tasks.create<Copy>(copyGeneratedFilesJsTask) {
                log("Copying ksp generated reactNative js files")

                from("${buildDir.path}/generated/ksp/metadata/commonMain/resources/js/")
                into("${baseDir}/react-native-${projectPackageName.replace(".", "-")}/src/")
            }

            if (teleresoKmp.disableReactExport) {
                log("Skipping adding reactNative tasks")
            } else {
                log("Adding reactNative tasks")

                // Android tasks
                tasks.getByName(copyGeneratedFilesAndroidTask)
                    .dependsOn("kspCommonMainKotlinMetadata")
                tasks.getByName(copyGeneratedFilesAndroidTask)
                    .finalizedBy(cleanAndroidGeneratedFiles)

                // iOS tasks
                tasks.getByName(copyGeneratedFilesIosTask)
                    .dependsOn("kspCommonMainKotlinMetadata")

                // Js tasks
                tasks.getByName(copyGeneratedFilesJsTask)
                    .dependsOn("kspCommonMainKotlinMetadata")

                dependsOnTasks.forEach {
                    tasks.findByName(it)?.dependsOn(cleanAndroidGeneratedFiles)
                    tasks.findByName(it)?.dependsOn(copyGeneratedFilesAndroidTask)
                    tasks.findByName(it)?.dependsOn(copyGeneratedFilesIosTask)
                    tasks.findByName(it)?.dependsOn(copyGeneratedFilesJsTask)
                }
            }
        }
    }
}

fun Project.getProjectName(): String {
    val extraKey = "projectPackageName"
    return when {
        extra.has(extraKey) -> extra.get(extraKey)
        rootProject.extra.has(extraKey) -> rootProject.extra.get(extraKey)
        else -> {
            log("`projectPackageName` was not found in project's extras will use project name instead ")
            name
        }
    }.toString()
}

fun Project.getScope(): String? {
    val extraKey = "scope"
    return when {
        extra.has(extraKey) -> extra.get(extraKey)
        rootProject.extra.has(extraKey) -> rootProject.extra.get(extraKey)
        else -> {
            log("`scope` was not found in project's extras, no scope will be added for js imports")
            null
        }
    }?.toString()
}

fun Project.log(message: String) {
    println("Telereso:$name: ${message}")
}

fun log(message: String) {
    println("Telereso: ${message}")
}