import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    google()
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("redacted") {
            id = "redacted"
            implementationClass = "de.mannodermaus.dk23.RedactedPlugin"
        }
    }
}

kotlin {
    explicitApi = ExplicitApiMode.Strict
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
    implementation(libs.asm)
    implementation(libs.kotlinx.metadata)
    compileOnly(libs.agp.gradle)
}
