package io.telereso.kmp

import com.google.devtools.ksp.gradle.KspGradleSubplugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.extra
import java.util.*

class KmpPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        project.pluginManager.apply(KspGradleSubplugin::class.java)
        project.dependencies.add("kspCommonMainMetadata", "io.telereso.kmp:processor:0.0.1")


        project.afterEvaluate {

            // Common tasks

            val jsCleanLibraryDistributionTask = "jsCleanLibraryDistribution"
            tasks.create<Delete>(jsCleanLibraryDistributionTask) {
                group = "Clean"
                delete(project.buildDir.resolve("productionLibrary"))
            }

            tasks.named("compileKotlinJs").configure {
                dependsOn(jsCleanLibraryDistributionTask)
            }

            when {

                // Models Tasks
                project.name.toLowerCase(Locale.getDefault()).endsWith("models") -> {

                    log("${project.name}: Creating Models Tasks")

                    // Models tasks

                    val copyGeneratedModelsTask = "kspCommonMainKotlinMetadataCopyGeneratedModels"
                    project.tasks.create<Copy>(copyGeneratedModelsTask) {
                        log("${project.name}: Copying ksp generated models")
                        group = "Ksp"
                        from("build/generated/ksp/metadata/commonMain/resources/kotlin/")
                        into("src/commonMain/kotlin/")
                    }


                    val cleanModelsGeneratedFilesTask = "kspCommonMainKotlinClean"

                    project.tasks.create<Delete>(cleanModelsGeneratedFilesTask) {
                        group = "Clean"
                        delete(project.fileTree("src").matching {
                            include("**/*.g.kt")
                        })
                    }


                    project.tasks.getByName(cleanModelsGeneratedFilesTask)
                        .dependsOn("kspCommonMainKotlinMetadata")
                    project.tasks.getByName(copyGeneratedModelsTask)
                        .dependsOn(cleanModelsGeneratedFilesTask)
                    project.tasks.getByName("preBuild").dependsOn(copyGeneratedModelsTask)
                    project.tasks.getByName("jsBrowserProductionLibraryDistribution")
                        .dependsOn(copyGeneratedModelsTask)


                }
                else -> {
                    // Lib tasks
                    val projectPackageName = getProjectName()

                    log("${project.name}: Creating Lib Tasks fro project $projectPackageName")

                    val copyGeneratedFilesAndroidTask = "kspCommonMainKotlinMetadataCopyGeneratedAndroid"

                    project.tasks.create<Copy>(copyGeneratedFilesAndroidTask) {
                        log("${project.name}: Copying ksp generated android files")

                        from("${project.buildDir.path}/generated/ksp/metadata/commonMain/kotlin/")
                        into("${project.rootProject.rootDir.path}/react-native-${projectPackageName.replace(".", "-")}/android/src/main/java/")

                    }


                    project.tasks.getByName(copyGeneratedFilesAndroidTask).dependsOn("kspCommonMainKotlinMetadata")
                    project.tasks.getByName("preBuild").dependsOn(copyGeneratedFilesAndroidTask)


                    val copyGeneratedFilesIosTask = "kspCommonMainKotlinMetadataCopyGeneratedIos"

                    project.tasks.create<Copy>(copyGeneratedFilesIosTask) {
                        log("${project.name}: Copying ksp generated ios files")

                        from("${project.buildDir.path}/generated/ksp/metadata/commonMain/resources/ios")
                        into("${project.rootProject.rootDir.path}/react-native-${projectPackageName.replace(".", "-")}/ios/")


                    }

                    project.tasks.getByName(copyGeneratedFilesIosTask).dependsOn("kspCommonMainKotlinMetadata")
                    project.tasks.getByName("preBuild").dependsOn(copyGeneratedFilesIosTask)
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


fun log(message: String) {
    println("Telereso: ${message}")
}