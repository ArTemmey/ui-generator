plugins {
    id("com.android.library")
    id("kotlin-parcelize")

    kotlin("plugin.serialization")
    kotlin("android")

    `maven-publish`
}

group = "com.github.ArtemiyDmtrvch"

android {
    compileSdk = 31
    buildToolsVersion = "31.0.0"

    defaultConfig {
        minSdk = 17
        targetSdk = 31

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

dependencies {
    implementation(project(":ui-generator-annotations"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.6.10")
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")
    implementation("com.github.ArtemiyDmtrvch:kotlin-delegate-concatenator:cf5890d227")
    api("org.jetbrains.kotlin:kotlin-reflect:1.6.10")
}
