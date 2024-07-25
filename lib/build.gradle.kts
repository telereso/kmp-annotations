import org.gradle.configurationcache.extensions.capitalized
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    alias(kmpLibs.plugins.android.library)

    alias(kmpLibs.plugins.kotlin.multiplatform)
    alias(kmpLibs.plugins.kotlin.native.cocoapods)
    alias(kmpLibs.plugins.kotlin.serialization)
    alias(kmpLibs.plugins.kotlin.parcelize)
    alias(kmpLibs.plugins.kotlinx.kover)
    alias(kmpLibs.plugins.dokka)

    alias(kmpLibs.plugins.sqldelight)
    alias(kmpLibs.plugins.test.logger)
    alias(kmpLibs.plugins.buildkonfig)
    alias(kmpLibs.plugins.telereso.kmp)
}


// Packaging
val groupId: String by rootProject.extra
val scope: String by rootProject.extra

group = "$groupId.${project.name}"
version = project.findProperty("publishVersion") ?: "0.0.1"


kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        // Required properties
        // Specify the required Pod version here. Otherwise, the Gradle project version is used.
        version = (project.properties["publishVersion"] ?: "0.0.1-place-holder") as String?
        summary = "Client sdk for feature"
        homepage = "https://www.annotations.com/"
        ios.deploymentTarget = "12.0"
        name = "AnnotationsClient"

        source = "${project.properties["artifactoryUrl"] ?: "https://test.com"}/mobile-cocoapods-local/$name/$version/$name.tar.gz"

        // Optional properties
        // Configure the Pod name here instead of changing the Gradle project name
        name = "AnnotationsClient"

        framework {
            baseName = "AnnotationsClient"

            // Optional properties
            // Dynamic framework support
            isStatic = false

            /**
             * working with multi-module. we need to export the external modules
             * run ./gradlew podPublishDebugXCFramework once completed
             * check the project podspec contains the exported
             */
            export(project(":annotations-models"))

            export(kmpLibs.telereso.core)


            // Dependency export
            //transitiveExport = false // This is default.
            // Bitcode embedding
            embedBitcode(org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode.BITCODE)
        }

        // Maps custom Xcode configuration to NativeBuildType
        //xcodeConfigurationToNativeBuildType["CUSTOM_DEBUG"] = org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.DEBUG
        //xcodeConfigurationToNativeBuildType["CUSTOM_RELEASE"] = org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.RELEASE
    }

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

    /**
     * Adding JS target to this lib. initially when creating this project, on Android studio the JS option is missing
     * for KKM Library.
     *
     */
    js {
        moduleName = "@$scope/${project.name}"
        version = project.version as String

        browser {
            testTask {
                useMocha()
            }
        }

        nodejs()

//        binaries.library()
        binaries.executable()
    }

    sourceSets {

        /**
         * https://kotlinlang.org/docs/opt-in-requirements.html#module-wide-opt-in
         * If you don't want to annotate every usage of APIs that require opt-in,
         * you can opt in to them for your whole module.
         * To opt in to using an API in a module, compile it with the argument -opt-in,
         * specifying the fully qualified name of the opt-in requirement annotation of the API you use
         */
        all {
            languageSettings.optIn("kotlin.js.ExperimentalJsExport")
        }

        commonMain {
            dependencies {
                implementation(project(":annotations"))
                api(project(":annotations-models"))
                api(kmpLibs.telereso.core)
                /**
                 * Add Ktor dependencies
                 * To use the Ktor client in common code, add the dependency to ktor-client-core to the commonMain
                 */
                implementation(kmpLibs.bundles.ktor)

                implementation(kmpLibs.bundles.sqldelight)

                implementation(kmpLibs.bundles.kotlinx)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(kmpLibs.test.kotlinx.coroutines.test)
                implementation(kmpLibs.bundles.test.kotest)
                implementation(kmpLibs.test.ktor.client.mock)
            }
        }
        jvmMain  {
            dependencies {
                implementation(kmpLibs.ktor.client.okhttp)
                implementation(kmpLibs.okhttp.logging)
                implementation(kmpLibs.sqldelight.runtime.jvm)
            }
        }

        jvmTest  {
            dependencies {
                implementation(kmpLibs.sqldelight.sqlite.driver)
            }
        }
        androidMain {
            dependencies {
                implementation(kmpLibs.ktor.client.okhttp)
                implementation(kmpLibs.okhttp.logging)
                implementation(kmpLibs.sqldelight.android.driver)
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(kmpLibs.sqldelight.sqlite.driver)
            }
        }

        iosMain {
            dependencies {
                /**
                 * For iOS, we add the ktor-client-darwin dependency
                 * Engines are used to process network requests. Note that a specific platform may require a specific engine that processes network requests.
                 */
                implementation(kmpLibs.ktor.client.darwin)

                implementation(kmpLibs.sqldelight.native.driver)
            }
        }


        /**
         * Adding main and test for JS.
         */
        jsMain {
            dependencies {
                /**
                 * Engines are used to process network requests. Note that a specific platform may require a specific engine that processes network requests.
                 */
                implementation(kmpLibs.ktor.client.js)

                implementation(kmpLibs.sqldelight.sqljs.driver)

//                implementation(devNpm("copy-webpack-plugin", "9.1.0"))
                implementation(npm("sql.js", kmpLibs.versions.sqlJs.get()))
                implementation(npm("@js-joda/core", kmpLibs.versions.js.joda.core.get()))
            }
        }
        jsTest {
            dependencies {
                implementation(kmpLibs.sqldelight.sqljs.driver)
            }
        }
    }

    /**
     * Since Kotlin 1.5.20
     * https://kotlinlang.org/docs/whatsnew1520.html#opt-in-export-of-kdoc-comments-to-generated-objective-c-headers
     * Note this is The ability to export KDoc comments to generated Objective-C headers is Experimental. It may be dropped or changed at any time
     * To try out this ability to export KDoc comments to Objective-C headers, use the -Xexport-kdoc compiler option. :)
     */
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilations.get("main").compilerOptions.options.freeCompilerArgs.add("-Xexport-kdoc")
    }
}

buildkonfig {
    //  Set the package name where BuildKonfig is being placed. Required.
    packageName = "$groupId.${project.name.replace("-",".")}"
    // objectName Set the name of the generated object. Defaults to BuildKonfig.
     objectName = "BuildKonfig"
    // exposeObjectWithName Set the name of the generated object, and make it public.
    // exposeObjectWithName = "YourAwesomePublicConfig"

    //defaultConfigs Set values which you want to have in common. Required.
    defaultConfigs {
        buildConfigField(STRING, "SDK_VERSION", "${project.properties["publishVersion"]?:"0.0.0"}")
        buildConfigField(STRING, "SDK_NAME", rootProject.name)
    }
}

/**
 * https://kotlin.github.io/dokka/1.6.0/user_guide/gradle/usage/
 */
tasks.dokkaHtml.configure {
    // Set module name displayed in the final output
    moduleName.set(rootProject.name.split("-").joinToString(" ") { it.capitalized() })

    outputDirectory.set(
        rootDir.resolve(
            "public${
                project.findProperty("publishVersion")?.let { "/$it" } ?: ""
            }"
        )
    )

    dokkaSourceSets {
        configureEach { // Or source set name, for single-platform the default source sets are `main` and `test`

            // Used when configuring source sets manually for declaring which source sets this one depends on
            // dependsOn("module")

            // Used to remove a source set from documentation, test source sets are suppressed by default
            //suppress.set(false)

            // Use to include or exclude non public members THIS IS DEPRACATED
            // includeNonPublic.set(true)

            /**
             * includeNonPublic is currently deprcated. recommened way to expose private or internal classes and funs is using this approach
             * we define the visbilites we are interersted in.
             * note this will make all private funs or clases or interfaces or val public on the doc level.
             * use suppress annotation to reomve any classes of fun you dont want part of the doc.
             * In our project we have classes with no package. the doc displats them in a root,. by rifght we chsould have a packages for each.
             */
            documentedVisibilities.set(
                setOf(
                    org.jetbrains.dokka.DokkaConfiguration.Visibility.PUBLIC, // Same for both Kotlin and Java
                    org.jetbrains.dokka.DokkaConfiguration.Visibility.PRIVATE, // Same for both Kotlin and Java
                    // DokkaConfiguration.Visibility.PROTECTED, // Same for both Kotlin and Java
                    org.jetbrains.dokka.DokkaConfiguration.Visibility.INTERNAL, // Kotlin-specific internal modifier
                    //  DokkaConfiguration.Visibility.PACKAGE, // Java-specific package-private visibility
                )
            )

            // Do not output deprecated members. Applies globally, can be overridden by packageOptions
            skipDeprecated.set(false)

            // Emit warnings about not documented members. Applies globally, also can be overridden by packageOptions
            reportUndocumented.set(true)

            // Do not create index pages for empty packages
            skipEmptyPackages.set(true)

            // This name will be shown in the final output
            // displayName.set("JVM")

            // Platform used for code analysis. See the "Platforms" section of this readme
            // platform.set(org.jetbrains.dokka.Platform.jvm)


            // Allows to customize documentation generation options on a per-package basis
            // Repeat for multiple packageOptions
            // If multiple packages match the same matchingRegex, the longuest matchingRegex will be used
//            perPackageOption {
//                matchingRegex.set("kotlin($|\\.).*") // will match kotlin and all sub-packages of it
//                // All options are optional, default values are below:
//                skipDeprecated.set(false)
//                reportUndocumented.set(true) // Emit warnings about not documented members
//                includeNonPublic.set(false)
//            }
            // Suppress a package
//            perPackageOption {
//                matchingRegex.set(""".*\.internal.*""") // will match all .internal packages and sub-packages
//                suppress.set(true)
//            }

            // Include generated files in documentation
            // By default Dokka will omit all files in folder named generated that is a child of buildDir
            //  suppressGeneratedFiles.set(false)
        }
    }
}

tasks.register<Copy>("copyiOSTestResources") {
    from("${rootDir}/lib/src/commonTest/resources")
    into("${rootDir}/lib/build/bin/iosSimulatorArm64/debugTest/resources")
}
tasks.findByName("iosSimulatorArm64Test")?.dependsOn("copyiOSTestResources")

/**
 * this task will run when ./gradlew iosSimulatorArm64Test testing Darwin test.
 * When we do not specificy the device ID, KMP will fallback to Iphone 12 which now
 * not supported by Xcode 14.
 * I set it to the latest supported which is the iPhone 14.
 */
tasks.named(
    "iosSimulatorArm64Test",
    org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest::class.java
).configure {
    device.set(kmpLibs.versions.test.iphone.device.get())
}

sqldelight {
    database("AnnotationsClientDatabase") {
        packageName = "$groupId.${project.name.replace("-",".")}.cache"

        sourceFolders = listOf("sqldelight")

        schemaOutputDirectory = file("src/commonMain/sqldelight/$groupId.${project.name.replace("-",".")}.cache")

        verifyMigrations = true
    }
}

android {
    namespace = "$groupId.${project.name.replace("-",".")}"
    compileSdk = kmpLibs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = kmpLibs.versions.minSdk.get().toInt()
    }
    resourcePrefix = "${rootProject.name.replace("-", "_")}_"
    compileOptions {
        sourceCompatibility = JavaVersion.valueOf("VERSION_${kmpLibs.versions.java.get()}")
        targetCompatibility = JavaVersion.valueOf("VERSION_${kmpLibs.versions.java.get()}")
    }
}

// We can filter out some classes in the generated report
koverReport {
    filters {
        excludes {
            classes(listOf())
        }
    }
    // The koverVerify currently only supports line counter values.
    // we can also configure this to run after the unit tests task.
    verify {
        // Add VMs in the includes [list]. VMs added,their coverage % will be tracked.
        filters {
            excludes {
                classes(listOf())
            }
        }
        // Enforce Test Coverage
        rule("Minimal line coverage rate in percent") {
            bound {
                minValue = 34
            }
        }
    }

    // We can configure the test results index.html to be stored anywhere within our propejct. normally its generated in the build folder
//    htmlReport {
//        reportDir.set(File("testresults"))
//    }
}

testlogger {

    theme =
        com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA // pick a theme - mocha, standard or plain
    showExceptions = true // show detailed failure logs
    showStackTraces = true
    showFullStackTraces =
        false // shows full exception stack traces,  useful to see the entirety of the stack trace.
    showCauses = true

    /**
     * sets threshold in milliseconds to highlight slow tests,
     * any tests that take longer than 0.5 seconds to run would have their durations logged using a warning style
     * and those that take longer than 1 seconds to run using an error style.
     */
    slowThreshold = 1000

    showSummary =
        true // displays a breakdown of passes, failures and skips along with total duration
    showSimpleNames = false
    showPassed = true
    showSkipped = true
    showFailed = true
    showOnlySlow = false
    /**
     * filter the log output based on the type of the test result.
     */
    showStandardStreams = true
    showPassedStandardStreams = true
    showSkippedStandardStreams = true
    showFailedStandardStreams = true

    logLevel = LogLevel.LIFECYCLE
}

teleresoKmp {
//    disableJsonConverters = true
    enableReactNativeExport = true
}