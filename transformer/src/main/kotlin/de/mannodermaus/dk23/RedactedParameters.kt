package de.mannodermaus.dk23

import com.android.build.api.instrumentation.InstrumentationParameters
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

/**
 * A set of configuration options that the custom transform allows.
 * This can be connected to a Gradle module's build script
 * to allow users to control what the transform does.
 *
 * This interface is connected directly to Gradle, so each parameter
 * must be annotated with one of the input annotations provided by it.
 */
public interface RedactedParameters : InstrumentationParameters {
    @get:Input
    public val enabled: Property<Boolean>
}
