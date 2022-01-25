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

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(android.sourceSets.getByName("main").java.srcDirs)
}

afterEvaluate {
    publishing {
        repositories {
            maven(url = "https://jitpack.io")
        }
        publications {
            create<MavenPublication>("debug") {
                // Applies the component for the release build variant.
                from(components["debug"])
                artifact(sourcesJar)
            }
            create<MavenPublication>("release") {
                // Applies the component for the release build variant.
                from(components["release"])
                artifact(sourcesJar)
            }
        }
    }
}

dependencies {
    implementation(project(":ui-generator-annotations"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version")
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")
    implementation("com.github.ArtemiyDmtrvch:kotlin-delegate-concatenator:cf5890d227")
    implementation("com.github.ArTemmey:singleton-entity:09dc93db63")
    api("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")
}