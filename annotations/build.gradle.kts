plugins {
  alias(kmpLibs.plugins.kotlin.multiplatform)
  alias(kmpLibs.plugins.android.library)
  id("maven-publish")
  id("convention.publication")
}

group = rootProject.group
version = rootProject.version

kotlin {
  jvm()

  android()

  iosX64()
  iosArm64()
  iosSimulatorArm64()

  watchosArm32()
  watchosArm64()
  watchosSimulatorArm64()

  js(IR) {
    browser()
    nodejs()
    binaries.library()
    binaries.executable()
  }

  sourceSets {

    all {
      languageSettings.optIn("kotlin.js.ExperimentalJsExport")
    }

    val commonMain by getting
    val commonTest by getting

    val androidMain by getting
    val androidTest by getting

    val iosX64Main by getting
    val iosArm64Main by getting
    val iosSimulatorArm64Main by getting

    val watchosArm32Main by getting
    val watchosArm64Main by getting

    val iosMain by creating {
      dependsOn(commonMain)
      iosX64Main.dependsOn(this)
      iosArm64Main.dependsOn(this)
      iosSimulatorArm64Main.dependsOn(this)

      watchosArm32Main.dependsOn(this)
      watchosArm64Main.dependsOn(this)
    }
    val iosX64Test by getting
    val iosArm64Test by getting
    val iosSimulatorArm64Test by getting

    val watchosArm32Test by getting
    val watchosArm64Test by getting
    val watchosSimulatorArm64Test by getting

    val iosTest by creating {
      dependsOn(commonTest)
      iosX64Test.dependsOn(this)
      iosArm64Test.dependsOn(this)
      iosSimulatorArm64Test.dependsOn(this)

      watchosArm32Test.dependsOn(this)
      watchosArm64Test.dependsOn(this)
      watchosSimulatorArm64Test.dependsOn(this)
    }

    val jvmMain by getting
    val jvmTest by getting

    val jsMain by getting
    val jsTest by getting
  }
}

android {
  namespace = "io.telereso.kmp.annotations"
  compileSdk = kmpLibs.versions.compileSdk.get().toInt()
  buildFeatures {
    buildConfig = false
  }
  defaultConfig {
    minSdk = kmpLibs.versions.minSdk.get().toInt()
  }
  compileOptions {
    sourceCompatibility = JavaVersion.valueOf("VERSION_${kmpLibs.versions.java.get()}")
    targetCompatibility = JavaVersion.valueOf("VERSION_${kmpLibs.versions.java.get()}")
  }
}

//////////////////// TODO remove this section with kotlin 1.9.0 ////////////////////
tasks.getByName("jsNodeProductionLibraryPrepare").dependsOn("jsProductionExecutableCompileSync")
tasks.getByName("jsBrowserProductionLibraryPrepare").dependsOn("jsProductionExecutableCompileSync")
tasks.getByName("jsBrowserProductionWebpack").dependsOn("jsProductionLibraryCompileSync")

tasks.getByName("signIosSimulatorArm64Publication")
  .dependsOn("publishIosArm64PublicationToMavenLocal")
  .dependsOn("publishIosArm64PublicationToSonatypeRepository")

tasks.getByName("signIosX64Publication")
  .dependsOn("publishIosArm64PublicationToMavenLocal")
  .dependsOn("publishIosSimulatorArm64PublicationToMavenLocal")
  .dependsOn("publishIosSimulatorArm64PublicationToSonatypeRepository")

tasks.getByName("signJsPublication")
  .dependsOn("publishIosArm64PublicationToMavenLocal")
  .dependsOn("publishIosSimulatorArm64PublicationToMavenLocal")
  .dependsOn("publishIosX64PublicationToMavenLocal")
  .dependsOn("publishIosX64PublicationToSonatypeRepository")

tasks.getByName("signJvmPublication")
  .dependsOn("publishIosArm64PublicationToMavenLocal")
  .dependsOn("publishIosSimulatorArm64PublicationToMavenLocal")
  .dependsOn("publishIosX64PublicationToMavenLocal")
  .dependsOn("publishJsPublicationToMavenLocal")
  .dependsOn("publishJsPublicationToSonatypeRepository")

tasks.getByName("signKotlinMultiplatformPublication")
  .dependsOn("publishIosArm64PublicationToMavenLocal")
  .dependsOn("publishIosSimulatorArm64PublicationToMavenLocal")
  .dependsOn("publishIosX64PublicationToMavenLocal")
  .dependsOn("publishJsPublicationToMavenLocal")
  .dependsOn("publishJvmPublicationToMavenLocal")
  .dependsOn("publishJvmPublicationToSonatypeRepository")

tasks.getByName("signWatchosArm32Publication")
  .dependsOn("publishIosSimulatorArm64PublicationToMavenLocal")
  .dependsOn("publishIosX64PublicationToMavenLocal")
  .dependsOn("publishJsPublicationToMavenLocal")
  .dependsOn("publishJvmPublicationToMavenLocal")
  .dependsOn("publishKotlinMultiplatformPublicationToMavenLocal")
  .dependsOn("publishJvmPublicationToSonatypeRepository")
  .dependsOn("publishKotlinMultiplatformPublicationToSonatypeRepository")

tasks.getByName("signWatchosArm64Publication")
  .dependsOn("publishIosX64PublicationToMavenLocal")
  .dependsOn("publishJsPublicationToMavenLocal")
  .dependsOn("publishJvmPublicationToMavenLocal")
  .dependsOn("publishKotlinMultiplatformPublicationToMavenLocal")
  .dependsOn("publishWatchosArm32PublicationToMavenLocal")
  .dependsOn("publishJvmPublicationToSonatypeRepository")
  .dependsOn("publishWatchosArm32PublicationToSonatypeRepository")

tasks.getByName("signWatchosSimulatorArm64Publication")
  .dependsOn("publishIosX64PublicationToMavenLocal")
  .dependsOn("publishJsPublicationToMavenLocal")
  .dependsOn("publishJvmPublicationToMavenLocal")
  .dependsOn("publishKotlinMultiplatformPublicationToMavenLocal")
  .dependsOn("publishWatchosArm32PublicationToMavenLocal")
  .dependsOn("publishWatchosArm64PublicationToMavenLocal")
  .dependsOn("publishJvmPublicationToSonatypeRepository")
  .dependsOn("publishWatchosArm32PublicationToSonatypeRepository")
  .dependsOn("publishWatchosArm64PublicationToSonatypeRepository")

//////////////////////////////////////////////////////////////////////////////////////////