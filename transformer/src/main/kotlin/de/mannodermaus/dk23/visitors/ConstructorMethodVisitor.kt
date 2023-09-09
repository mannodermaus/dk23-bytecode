package de.mannodermaus.dk23.visitors

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor

private const val REDACTED_ANNOTATION = "Lde/mannodermaus/dk23/Redacted;"

/**
 * This visitor collects the indices of each parameter of a method
 * if it's annotated with @Redacted. After collecting everything,
 * the [onResult] function is called to provide the list of indices
 * (which may be empty) to the caller.
 */
internal class ConstructorMethodVisitor(
    api: Int,
    next: MethodVisitor,
    private val onResult: (List<Int>) -> Unit,
) : MethodVisitor(api, next) {

    private var indices = mutableListOf<Int>()

    override fun visitParameterAnnotation(
        parameter: Int,
        descriptor: String?,
        visible: Boolean
    ): AnnotationVisitor {
        if (descriptor == REDACTED_ANNOTATION) {
            indices += parameter
        }

        return super.visitParameterAnnotation(parameter, descriptor, visible)
    }

    override fun visitEnd() {
        super.visitEnd()

        onResult(indices)
    }
}
