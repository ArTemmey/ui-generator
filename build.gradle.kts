import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("base")
}

buildscript {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath(kotlin("gradle-plugin", kotlin_version))
        classpath(kotlin("serialization", kotlin_version))
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven(url = "https://jitpack.io")
    }
}

tasks.getByName("clean", Delete::class) {
    delete(rootProject.buildDir)
}

tasks.withType(KotlinCompile::class) {
    kotlinOptions {
        jvmTarget = java_version.toString()
    }
}