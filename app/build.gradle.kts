plugins {
    id("com.google.devtools.ksp") version ksp_version
    id("com.android.application")

    kotlin("plugin.serialization")
    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdk = 31
    buildToolsVersion = "31.0.0"

    defaultConfig {
        applicationId = "ru.impression.ui_generator_example"
        minSdk = 17
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    dataBinding {
        addKtx = true
        isEnabled = true
    }
    kotlinOptions {
        jvmTarget = java_version.toString()
    }
}

java {
    sourceCompatibility = java_version
    targetCompatibility = java_version
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/debug/kotlin")
    }
}

ksp {
    arg("packageName","ru.impression.ui_generator_example")
}

dependencies {
    implementation(project(":ui-generator-base"))
    implementation(project(":ui-generator-annotations"))
    ksp(project(":ui-generator-processor"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")
}
