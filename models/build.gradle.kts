plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.telereso)
}

// Setup extras variables
val groupId: String by rootProject.extra
val scope: String by rootProject.extra

// Setup publish variables
val baseProjectName = rootProject.name.replace("-client", "")
project.ext["artifactName"] = project.name

group = "$groupId.${project.name}"
version = project.findProperty("publishVersion") ?: "0.0.1"


kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    /**
     * using jvm for Android targets inorder for the jvm jar file to be created.
     * this is needed for the MavenLocal dependecny.
     * only issue Android SDK specific implementaations wont work. This is still work in prgress.
     */
    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }


    js(IR) {
        moduleName = project.name

        compilations["main"].packageJson {
            name = "@$scope/$moduleName"
            version = project.version as String
            customField("buildTimeStamp", "${System.currentTimeMillis()}")
        }
        /**
         * browser()
         * It sets the JavaScript target execution environment as browser.
         * It provides a Gradle task—jsBrowserTest that runs all js tests inside the browser using karma and webpack.
         */
        browser()
        /**
         * nodejs()
         * It sets the JavaScript target execution environment as nodejs.
         * It provides a Gradle task—jsNodeTest that runs all js tests inside nodejs using the built-in test framework.
         */
        nodejs()
        /**
         * binaries.library()
         * It tells the Kotlin compiler to produce Kotlin/JS code as a distributable node library.
         * Depending on which target you've used along with this,
         * you would get Gradle tasks to generate library distribution files
         */
        binaries.library()
        /**
         * binaries.executable()
         * it tells the Kotlin compiler to produce Kotlin/JS code as webpack executable .js files.
         */
        binaries.executable()
    }

    sourceSets {

        all {
            languageSettings.optIn("kotlin.js.ExperimentalJsExport")
        }

        val commonMain by getting {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                api(libs.telereso.core)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting
        val androidUnitTest by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }

        val jvmMain by getting
        val jvmTest by getting
        val jsMain by getting
        val jsTest by getting
    }
}

android {
    namespace = (group as String).replace("-", ".")
    compileSdk = libs.versions.compileSdk.get().toInt()
    buildFeatures {
        buildConfig = false
    }
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    publishing {
        multipleVariants {
            withSourcesJar()
            withJavadocJar()
            allVariants()
        }
    }
}

tasks.dokkaHtml.configure {
    // Set module name displayed in the final output
    moduleName.set(project.name)
    outputDirectory.set(
        rootDir.resolve(
            "public${
                project.findProperty("publishVersion")?.let { "/" } ?: ""
            }/models"
        )
    )
}

teleresoKmp {
    swiftOverloadsByJvmOverloads = true
    createObjectFunctionName = "instance"
}
