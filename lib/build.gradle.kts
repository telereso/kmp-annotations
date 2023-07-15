import org.gradle.configurationcache.extensions.capitalized
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    alias(libs.plugins.android.library)

    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.native.cocoapods)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.dokka)

    alias(libs.plugins.sqldelight)
    alias(libs.plugins.test.logger)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.telereso)
}


// Packaging
val groupId: String by rootProject.extra
val scope: String by rootProject.extra

group = "$groupId.${project.name}"
version = project.findProperty("publishVersion") ?: "0.0.1"

val disableJsTarget: String? by project

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

            export(libs.telereso.core)


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

    if (!disableJsTarget.toBoolean()) {
        js(IR) {
            moduleName = "annotations-client"

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
            browser {
                testTask {
                    useMocha()
                }
            }
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

        val commonMain by getting {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                implementation(project(":annotations"))
                api(project(":annotations-models"))
                api(libs.telereso.core)
                /**
                 * Add Ktor dependencies
                 * To use the Ktor client in common code, add the dependency to ktor-client-core to the commonMain
                 */
                implementation(libs.bundles.ktor)

                implementation(libs.bundles.sqldelight)

                implementation(libs.bundles.kotlinx)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(libs.kotlinx.coroutines.test)
                implementation("io.kotest:kotest-framework-engine:5.5.3")
                implementation("io.kotest:kotest-assertions-core:5.5.3")

                implementation(libs.ktor.client.mock)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(libs.okhttp.logging)
                implementation(libs.sqldelight.runtime.jvm)
            }
        }

        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                implementation(libs.sqldelight.sqlite.driver)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(libs.okhttp.logging)
                implementation(libs.sqldelight.android.driver)
            }
        }
        val androidUnitTest by getting {
            dependsOn(commonTest)
            dependencies {
                implementation(libs.sqldelight.sqlite.driver)
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting

        /**
         * By using the by creating scope, we ensure the rest of the Darwin targets
         * pick dependecies from the iOSMain.
         * Note using this actual implementations should only exist in the iosMain else
         * the project will complain.
         */
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                /**
                 * For iOS, we add the ktor-client-darwin dependency
                 * Engines are used to process network requests. Note that a specific platform may require a specific engine that processes network requests.
                 */
                implementation(libs.ktor.client.darwin)

                implementation(libs.sqldelight.native.driver)
            }
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting {
            dependsOn(commonTest)
        }
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            /**
             * TO runs tests for iOS the simulator should not depend on ioSTEst to avoid duplication.
             */
            //iosSimulatorArm64Test.dependsOn(this)
        }

        if (!disableJsTarget.toBoolean()) {

            val jsMain by getting {
                dependencies {
                    /**
                     * Engines are used to process network requests. Note that a specific platform may require a specific engine that processes network requests.
                     */
                    implementation(libs.ktor.client.js)

                    implementation(libs.sqldelight.sqljs.driver)

//                implementation(devNpm("copy-webpack-plugin", "9.1.0"))
                    implementation(npm("sql.js", libs.versions.sqlJs.get()))
                    implementation(npm("@js-joda/core", libs.versions.js.joda.core.get()))
                }
            }

            val jsTest by getting {
                dependsOn(commonTest)
                dependencies {
                    implementation(libs.sqldelight.sqljs.driver)
                }
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
        compilations.get("main").kotlinOptions.freeCompilerArgs += "-Xexport-kdoc"
    }
}

buildkonfig {
    //  Set the package name where BuildKonfig is being placed. Required.
    packageName = "$groupId.${project.name.replace("-",".")}"
    // objectName Set the name of the generated object. Defaults to BuildKonfig.
    // objectName = "YourAwesomeConfig"
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
    deviceId = "iPhone 14 Pro"
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
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    resourcePrefix = "${rootProject.name.replace("-", "_")}_"
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

// We can filter out some classes in the generated report
kover {
    filters {
        classes {
            //includes += listOf("*.*ViewModelImpl*", "$groupId..cache.*")
            //excludes += listOf("*.*Genre*", "$groupId..cache.*")
        }
    }
    // The koverVerify currently only supports line counter values.
    // we can also configure this to run after the unit tests task.
    verify {
        // Add VMs in the includes [list]. VMs added,their coverage % will be tracked.
        filters {
            classes {
                //includes += listOf("*.*ViewModelImpl*", "$groupId..cache.*")
                //excludes += listOf("*.*Genre*", "$groupId..cache.*")
            }
        }
        // Enforce Test Coverage
        rule {
            name = "Minimal line coverage rate in percent"
            bound {
                minValue = 40
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
//    disableReactExport = true
}