@file:Suppress("UnstableApiUsage")

package de.mannodermaus.dk23

import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType

/**
 * A Gradle plugin that registers a custom bytecode transform.
 */
public class RedactedPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Configuration parameters for the Gradle plugin
        val extension = project.extensions.create(
            "redacted",
            RedactedParameters::class.java,
        )

        // Connect to AGP via its 'androidComponents' block
        project.plugins.withType<AppPlugin> {
            val androidComponents =
                project.extensions.getByType(AndroidComponentsExtension::class.java)

            androidComponents.onVariants { variant ->
                variant.instrumentation.transformClassesWith(
                    RedactedClassVisitorFactory::class.java,
                    InstrumentationScope.PROJECT,
                ) { params ->
                    // Connect Gradle plugin parameters to the transform
                    params.enabled.set(extension.enabled.orElse(true))
                }
            }
        }
    }
}
