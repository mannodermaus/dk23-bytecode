package de.mannodermaus.dk23

// The custom transform will make add this annotation
// to every class with at least one @Redacted field behind the scenes.
// For this demonstration, there is no purpose behind this,
// but it serves as an example of what the Transform API can do.
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class RedactedMarker
