// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()  // Agrega mavenCentral() si no está incluido
    }
    dependencies {
        classpath(libs.androidx.navigation.safe.args.gradle.plugin)  // Actualizado a la versión más reciente
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}