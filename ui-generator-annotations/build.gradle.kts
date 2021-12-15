plugins {
    kotlin("jvm")

    `maven-publish`
}

group = "com.github.ArtemiyDmtrvch"

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