plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.library)
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
  compileSdk = libs.versions.compileSdk.get().toInt()
  buildFeatures {
    buildConfig = false
  }
  defaultConfig {
    minSdk = libs.versions.minSdk.get().toInt()
  }
  compileOptions {
    sourceCompatibility = JavaVersion.valueOf("VERSION_${libs.versions.java.get()}")
    targetCompatibility = JavaVersion.valueOf("VERSION_${libs.versions.java.get()}")
  }
}
tasks.findByName("jsNodeProductionLibraryPrepare")?.dependsOn("jsProductionExecutableCompileSync")
tasks.findByName("jsBrowserProductionLibraryPrepare")?.dependsOn("jsProductionExecutableCompileSync")
tasks.findByName("jsBrowserProductionWebpack")?.dependsOn("jsProductionLibraryCompileSync")