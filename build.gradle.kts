import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  //trick: for the same plugin versions in all sub-modules
  id("com.android.application").version("7.3.1").apply(false)
  id("com.android.library").version("7.3.1").apply(false)
  id("org.jetbrains.kotlin.android").version("1.7.21").apply(false)
  id ("org.jetbrains.kotlin.plugin.parcelize").version("1.7.21").apply(false)
  kotlin("multiplatform").version("1.7.21").apply(false)
}

group = "io.telereso.kmp"
version = project.findProperty("publishVersion") ?: "0.0.1"

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

