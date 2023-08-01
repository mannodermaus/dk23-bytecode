plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion(libs.versions.kotlin.get())
        }
    }
}

dependencies {
    implementation(gradleApi())
    implementation(libs.agp.gradle.api)
}
