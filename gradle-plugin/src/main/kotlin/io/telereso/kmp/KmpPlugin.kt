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
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.extra
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension
import java.io.File
import java.util.*
import kotlin.jvm.optionals.getOrNull

const val KEY_TELERESO_KMP_DEVELOPMENT_MODE = "teleresoKmpDevelopmentPath"
const val KEY_TELERESO_KMP_VERSION = "teleresoKmpVersion"
const val KEY_TELERESO_CATALOG_NAME = "teleresoCatalogName"


class KmpPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {

        pluginManager.apply(KspGradleSubplugin::class.java)

        val kspExtension = extensions.getByType(KspExtension::class.java)
        val scope = getScope()?.let { scope ->
            kspExtension.arg("scope", scope)
            scope
        }

        val localProps = Properties().apply {
            File("${rootProject.rootDir}/local.properties").apply {
                if (exists())
                    inputStream().use { fis ->
                        load(fis)
                    }
            }
        }

        val catalogNameLocalProp = localProps[KEY_TELERESO_CATALOG_NAME]?.toString()
        val catalogNameProjectProp = findProperty(KEY_TELERESO_CATALOG_NAME)?.toString()
        val libs = extensions.findByType(VersionCatalogsExtension::class.java)
            ?.named(catalogNameLocalProp ?: catalogNameProjectProp ?: "kmpLibs")


        val devModeLocalProp = localProps[KEY_TELERESO_KMP_DEVELOPMENT_MODE]?.toString()
        val devModeProjectProp = findProperty(KEY_TELERESO_KMP_DEVELOPMENT_MODE)?.toString()

        if (!(devModeLocalProp ?: devModeProjectProp).isNullOrBlank()
            && findProperty("publishGradlePlugin")?.toString()?.toBoolean() != true
        ) {
            log("Using local projects :annotations and :processor")
            dependencies.add("commonMainImplementation", project(":annotations"))
            dependencies.add("kspCommonMainMetadata", project(":processor"))
        } else {
            val annotationsVersion = libs?.findVersion("teleresoKmp")?.getOrNull() ?: findProperty(KEY_TELERESO_KMP_VERSION)?.toString()
            dependencies.add("commonMainImplementation", "io.telereso.kmp:annotations:$annotationsVersion")
            dependencies.add("kspCommonMainMetadata", "io.telereso.kmp:processor:$annotationsVersion")
        }

        val teleresoKmp = teleresoKmp()


        fun includeGeneratedClassesToSrcSet(kotlinMultiplatformExtension: KotlinMultiplatformExtension?) {
            kotlinMultiplatformExtension
                ?.sourceSets
                ?.findByName("commonMain")
                ?.kotlin {
                    srcDirs(buildDir.resolve("generated/ksp/metadata/commonMain/kotlin"))
                }
        }

        afterEvaluate {
            kspExtension.arg(
                "swiftOverloadsByJvmOverloads",
                teleresoKmp.swiftOverloadsByJvmOverloads.toString()
            )
            kspExtension.arg("createObjectFunctionName", teleresoKmp.createObjectFunctionName)

            val kotlinMultiplatformExtension = project.extensions
                .findByType(KotlinMultiplatformExtension::class.java)

            if (teleresoKmp.enableReactNativeExport || !teleresoKmp.disableJsonConverters) {
                includeGeneratedClassesToSrcSet(kotlinMultiplatformExtension)
            }

            // Common tasks


            val jsCleanLibraryDistributionTask = "jsCleanLibraryDistribution"
            tasks.create<Delete>(jsCleanLibraryDistributionTask) {
                group = "Clean"
                delete(buildDir.resolve("dist/js/productionLibrary"))
            }

            tasks.findByName("compileKotlinJs")?.apply {
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

            tasks.findByName("compileReleaseKotlinAndroid")
                ?.dependsOn("kspCommonMainKotlinMetadata")
            tasks.findByName("compileDebugKotlinAndroid")?.dependsOn("kspCommonMainKotlinMetadata")
            tasks.findByName("compileKotlinJvm")?.dependsOn("kspCommonMainKotlinMetadata")
            tasks.findByName("compileKotlinIosArm64")?.dependsOn("kspCommonMainKotlinMetadata")
            tasks.findByName("compileKotlinIosSimulatorArm64")
                ?.dependsOn("kspCommonMainKotlinMetadata")
            tasks.findByName("compileKotlinIosX64")?.dependsOn("kspCommonMainKotlinMetadata")
            tasks.findByName("compileKotlinJs")?.dependsOn("kspCommonMainKotlinMetadata")
            tasks.findByName("jsSourcesJar")?.dependsOn("kspCommonMainKotlinMetadata")
            tasks.findByName("iosArm64SourcesJar")?.dependsOn("kspCommonMainKotlinMetadata")
            tasks.findByName("sourcesJar")?.dependsOn("kspCommonMainKotlinMetadata")
            tasks.findByName("iosX64SourcesJar")?.dependsOn("kspCommonMainKotlinMetadata")
            tasks.findByName("iosSimulatorArm64SourcesJar")?.dependsOn("kspCommonMainKotlinMetadata")
            tasks.findByName("jvmSourcesJar")?.dependsOn("kspCommonMainKotlinMetadata")

//            handleGradle8DokkaTasks()

            tasks.findByName("jsNodeProductionLibraryPrepare")
                ?.dependsOn("jsProductionExecutableCompileSync")
            tasks.findByName("jsBrowserProductionLibraryPrepare")
                ?.dependsOn("jsProductionExecutableCompileSync")
            tasks.findByName("jsBrowserProductionWebpack")
                ?.dependsOn("jsProductionLibraryCompileSync")

            if (teleresoKmp.disableJsonConverters) {
                log("Skipping adding models tasks")
            } else {
                log("Adding Models Tasks")
                includeGeneratedClassesToSrcSet(kotlinMultiplatformExtension)
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
                delete(buildDir.resolve("generated/ksp/metadata/commonMain/rn-kotlin"))
            }

            tasks.create<Copy>(copyGeneratedFilesAndroidTask) {
                log("Copying ksp generated reactNative android files")

                from("${buildDir.path}/generated/ksp/metadata/commonMain/resources/rn-kotlin/")
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

            if (!teleresoKmp.enableReactNativeExport) {
                log("Skipping adding reactNative tasks")
            } else {
                log("Adding reactNative tasks")
                val reactNativeDir = "${baseDir}/react-native-${projectPackageName.replace(".", "-")}"

                // Android tasks
                tasks.getByName(copyGeneratedFilesAndroidTask)
                    .dependsOn("kspCommonMainKotlinMetadata")
//                tasks.getByName(copyGeneratedFilesAndroidTask)
//                    .finalizedBy(cleanAndroidGeneratedFiles)

                val copyAndroidExampleGradle = "copyAndroidExampleGradle"
                tasks.create(copyAndroidExampleGradle) {
                    copy {
                        from("${baseDir}/gradle/")
                        into("$reactNativeDir/example/android/gradle/")
                    }

                    copy {
                        from("${baseDir}/local.properties")
                        into("${reactNativeDir}/example/android/")
                    }
                }

                // iOS tasks
                tasks.getByName(copyGeneratedFilesIosTask)
                    .dependsOn("kspCommonMainKotlinMetadata")

                // Js tasks
                tasks.getByName(copyGeneratedFilesJsTask)
                    .dependsOn("kspCommonMainKotlinMetadata")

                // Workaround to support gradle 8 and java 17 with kotlin 1.8
                val reactNativeGradle8Workaround = "reactNativeGradle8Workaround"
                tasks.create(reactNativeGradle8Workaround) {
                    listOf(
                        rootDir.resolve(
                            "${baseDir}/react-native-${
                                projectPackageName.replace(
                                    ".",
                                    "-"
                                )
                            }/example/node_modules/react-native-gradle-plugin/build.gradle.kts"
                        ),
                        rootDir.resolve(
                            "${baseDir}/react-native-${
                                projectPackageName.replace(
                                    ".",
                                    "-"
                                )
                            }/node_modules/react-native-gradle-plugin/build.gradle.kts"
                        )
                    ).forEach { f ->
                        if (f.exists()) {
                            log("Applying $reactNativeGradle8Workaround on: ")
                            val content = f.readText()
                                .replace(
                                    """kotlin("jvm") version "1.6.10"""",
                                    """kotlin("jvm") version "1.9.10""""
                                ).replace(
                                    "JavaVersion.VERSION_11",
                                    "JavaVersion.VERSION_17"
                                ).replace(
                                    "JavaVersion.VERSION_1_8",
                                    "JavaVersion.VERSION_17"
                                )
                            f.writeText(content)
                        }
                    }
                }

                val exportReactNativePackages = "exportReactNativePackages"
                val cocoapods = kotlinMultiplatformExtension?.getCocoapods()
                exportReactNativePackages(exportReactNativePackages, teleresoKmp, cocoapods)

                dependsOnTasks.forEach {
                    tasks.findByName(it)?.dependsOn(cleanAndroidGeneratedFiles)
                    tasks.findByName(it)?.dependsOn(copyAndroidExampleGradle)
                    tasks.findByName(it)?.dependsOn(copyGeneratedFilesAndroidTask)
                    tasks.findByName(it)?.dependsOn(copyGeneratedFilesIosTask)
                    tasks.findByName(it)?.dependsOn(copyGeneratedFilesJsTask)
                    if (!teleresoKmp.disableReactNativeGradle8Workaround)
                        tasks.findByName(it)?.dependsOn(reactNativeGradle8Workaround)
                }
            }

            if (!teleresoKmp.enableFlutterExport) {
                log("Skipping adding flutter tasks")
            } else {
                log("Adding flutter tasks")
                val flutterDir = "${baseDir}/flutter_${projectPackageName.replace("-", "_")}"

                // Android tasks
                val copyFlutterGeneratedFilesAndroidTask = "copyFlutterGeneratedFilesAndroidTask"
                tasks.create<Copy>(copyFlutterGeneratedFilesAndroidTask) {
                    log("Copying ksp generated flutter android files")

                    from("${buildDir.path}/generated/ksp/metadata/commonMain/resources/flutter-kotlin/")
                    into(
                        "${baseDir}/flutter_${
                            projectPackageName.replace(
                                ".",
                                "_"
                            )
                        }/android/src/main/kotlin/"
                    )

                }

                val copyFlutterAndroidExampleGradle = "copyFlutterAndroidExampleGradle"
                tasks.create(copyFlutterAndroidExampleGradle) {
//                    copy {
//                        from("${baseDir}/gradle/")
//                        into("$flutterDir/example/android/gradle/")
//                    }
//
//                    copy {
//                        from("${baseDir}/local.properties")
//                        into("${flutterDir}/example/android/")
//                    }
                }

                // iOS tasks
                val copyFlutterGeneratedFilesIosTask = "copyFlutterGeneratedFilesIosTask"
                tasks.create<Copy>(copyFlutterGeneratedFilesIosTask) {
                    log("Copying ksp generated flutter ios files")

                    from("${buildDir.path}/generated/ksp/metadata/commonMain/resources/flutter")
                    into(
                        "${baseDir}/flutter_${
                            projectPackageName.replace(
                                ".",
                                "_"
                            )
                        }/flutter/"
                    )
                }
                tasks.getByName(copyFlutterGeneratedFilesIosTask)
                    .dependsOn("kspCommonMainKotlinMetadata")

                // Js
                val copyFlutterGeneratedFilesTask = "copyFlutterGeneratedFilesTask"

                tasks.create<Copy>(copyFlutterGeneratedFilesTask) {
                    log("Copying ksp generated flutter files")

                    from("${buildDir.path}/generated/ksp/metadata/commonMain/resources/flutter/")
                    into("${baseDir}/flutter_${projectPackageName.replace(".", "_")}/src/")
                }

//                dependsOnTasks.forEach {
//                    tasks.findByName(it)?.dependsOn(cleanAndroidGeneratedFiles)
//                    tasks.findByName(it)?.dependsOn(copyAndroidExampleGradle)
//                    tasks.findByName(it)?.dependsOn(copyGeneratedFilesAndroidTask)
//                    tasks.findByName(it)?.dependsOn(copyGeneratedFilesIosTask)
//                    tasks.findByName(it)?.dependsOn(copyGeneratedFilesJsTask)
//                }
            }
        }

        gradle.projectsEvaluated {
            tasks.findByName("androidReleaseSourcesJar")?.dependsOn("kspCommonMainKotlinMetadata")
        }
    }

    private fun Project.exportReactNativePackages(
        exportReactNativePackagesTask: String,
        teleresoKmp: TeleresoKmpExtension,
        cocoapodsExtension: CocoapodsExtension?
    ) {
        log("Processing ReactNative packages: ${teleresoKmp.exportedReactNativePackages.joinToString { name }}")

        var taskEnabled = teleresoKmp.exportedReactNativePackages.isNotEmpty()
                && cocoapodsExtension != null
                && !project.plugins.hasPlugin("com.android.application")

        val rnpDir = teleresoKmp.reactNativePackageDirectory
            ?: rootDir.resolve("react-native-${project.name}")
        if (!rnpDir.exists()) {
            log("React Native package not found!, set it using `reactNativePackageDirectory` or rename dir to `react-native-${project.name}`, caused by setting packages ${teleresoKmp.exportedReactNativePackages}")
            taskEnabled = false
        }


        val frameworkName = teleresoKmp.umbrellaFrameworkName ?: cocoapodsExtension?.name

        log("Detect frameworkName: $frameworkName, if not correct try setting `umbrellaFrameworkName`")

        val nodeModulesDir =
            teleresoKmp.nodeModulesDirectory ?: rnpDir.resolve("node_modules")

        val exportedDir = rnpDir.resolve("ios/Exported")

        tasks.create(exportReactNativePackagesTask) {
            enabled = taskEnabled
            doFirst {
                if (cocoapodsExtension == null) {
                    log("Failed to locate CocoaPods for this project, make sure to implement it first before exporting packages")
                }
                if (exportedDir.exists()) exportedDir.deleteRecursively()
                exportedDir.mkdir()

                teleresoKmp.exportedReactNativePackages.forEach { rnp ->
                    val rnpFolder = nodeModulesDir.resolve(rnp.path)
                    if (!rnpFolder.exists()) throw RuntimeException("exported package not found $rnp at path $rnpFolder")

                    val iosDir = nodeModulesDir.resolve(rnp.pathIos)

                    val exportedPackageDir =
                        exportedDir.resolve(rnp.name.removePrefix("react-native").toPascal())
                    exportedPackageDir.mkdir()

                    copy {
                        from(iosDir.path)
                        into(exportedPackageDir.path)
                        eachFile {
                            if (relativePath.segments.size > 1) {
                                exclude()
                            }
                            if (path.contains(".xcodeproj")
                                || path.contains(".xcworkspace")
                                || (!name.endsWith(".h")
                                        && !name.contains(".m")
                                        && !name.contains(".swift"))
                            ) {
                                exclude()
                            }
                        }

                    }
                }
            }

            doLast {
                project.fileTree(exportedDir).visit {
                    if (isDirectory && file.listFiles().isEmpty()
                        || file.name.endsWith(".xcodeproj")
                        || file.name.endsWith(".xcworkspace")
                    ) {
                        file.deleteRecursively()
                    }
                }
            }
        }

        val replaceImportedReactNativeFramework = "replaceImportedReactNativeFramework"
        tasks.create(replaceImportedReactNativeFramework) {
            enabled = taskEnabled
            doLast {
                project.fileTree(exportedDir).visit {
                    if (file.name.endsWith(".swift")) {
                        var content = file.readText()

                        if (teleresoKmp.removeStringErrorExtension)
                            content = content.replace(
                                "extension String: Error {\n}", ""
                            )

                        val detectedFrameworkName =
                            """@objc\(([^)]+)\)""".toRegex().find(content)?.groups?.get(1)?.value

                        teleresoKmp.exportedReactNativePackages.forEach { rnp ->
                            (rnp.framework ?: detectedFrameworkName)?.let { oldFramework ->
                                content = content.replace(
                                    "import $oldFramework",
                                    "import $frameworkName"
                                )
                            }
                        }
                        file.writeText(content)
                    }
                }
            }
        }

        tasks.getByName(exportReactNativePackagesTask)
            .dependsOn("kspCommonMainKotlinMetadata")
            .finalizedBy(replaceImportedReactNativeFramework)
    }
}

private fun KotlinMultiplatformExtension.getCocoapods(): CocoapodsExtension? {
    return (this as? org.gradle.api.plugins.ExtensionAware)?.extensions?.findByName("cocoapods") as? CocoapodsExtension
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