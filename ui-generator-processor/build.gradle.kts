plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "com.github.ArtemiyDmtrvch"

dependencies {
    implementation(project(":ui-generator-annotations"))
    implementation("com.squareup:kotlinpoet:1.9.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")
    implementation("com.google.devtools.ksp:symbol-processing-api:$ksp_version")
}

val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}

publishing {
    repositories {
        maven(url = "https://jitpack.io")
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}