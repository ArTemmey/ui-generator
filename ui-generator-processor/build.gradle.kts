plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":ui-generator-annotations"))
    implementation("com.squareup:kotlinpoet:1.5.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")
    implementation("com.google.devtools.ksp:symbol-processing-api:$ksp_version")
}