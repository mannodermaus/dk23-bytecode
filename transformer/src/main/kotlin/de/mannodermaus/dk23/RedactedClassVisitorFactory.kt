package de.mannodermaus.dk23

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import de.mannodermaus.dk23.ext.isDataClass
import de.mannodermaus.dk23.visitors.ConstructorMethodVisitor
import de.mannodermaus.dk23.visitors.KotlinMetadataAnnotationVisitor
import de.mannodermaus.dk23.visitors.ToStringMethodVisitor
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

private const val CONSTRUCTOR_METHOD_NAME = "<init>"
private const val TO_STRING_METHOD_NAME = "toString"

private const val KT_METADATA_DESCRIPTOR = "Lkotlin/Metadata;"
private const val REDACTED_MARKER_DESCRIPTOR = "Lde/mannodermaus/dk23/RedactedMarker;"
private const val REDACTED_MARKER_ANNOTATION = "de.mannodermaus.dk23.RedactedMarker"

/**
 * A custom transform implementation to manipulate the bytecode of classes with @Redacted annotations.
 *
 * Using the Visitor API of the ASM library, it instruments a target class in the following ways:
 *   - Obfuscate the clear-text value of each @Redacted parameter from its `toString()` representation
 *   - Add the @RedactedMarker annotation to the class
 */
public abstract class RedactedClassVisitorFactory : AsmClassVisitorFactory<RedactedParameters> {

    override fun isInstrumentable(classData: ClassData): Boolean {
        // When the transform is completely turned off, don't bother
        if (!enabled) return false

        // Skip over classes with the internal 'redacted marker' annotation,
        // since those were already processed by this transform
        return classData.classAnnotations.none { it == REDACTED_MARKER_ANNOTATION }
    }

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor = RedactedClassVisitor(
        api = instrumentationContext.apiVersion.get(),
        next = nextClassVisitor,
    )

    /* Private */

    private val enabled get() = parameters.get().enabled.get()
}

private class RedactedClassVisitor(
    api: Int,
    next: ClassVisitor,
) : ClassVisitor(api, next) {

    private var redactedParameterIndices = emptyList<Int>()
    private var isDataClass = false

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
        val next = super.visitAnnotation(descriptor, visible)

        return if (descriptor == KT_METADATA_DESCRIPTOR) {
            // Try to read Kotlin @Metadata to see if we're dealing with a `data class`
            // (for this, use the kotlinx.metadata library to interpret the metadata)
            KotlinMetadataAnnotationVisitor(api, next) { metadata ->
                isDataClass = metadata?.isDataClass ?: false
            }
        } else {
            next
        }
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val next = super.visitMethod(access, name, descriptor, signature, exceptions)

        // Abort early when not dealing with a data class
        if (!isDataClass) return next

        // Otherwise, hook into several different methods and instrument them
        return when (name) {
            CONSTRUCTOR_METHOD_NAME -> ConstructorMethodVisitor(
                api = api,
                next = next,
                onResult = { indices -> redactedParameterIndices = indices },
            )

            TO_STRING_METHOD_NAME -> ToStringMethodVisitor(
                api = api,
                next = next,
                redactedParameterIndices = redactedParameterIndices,
            )

            else -> next
        }
    }

    override fun visitEnd() {
        super.visitEnd()

        // For classes where the transform actually manipulated the bytecode,
        // attach the `RedactedMarker` annotation to the target class
        // by calling its visit() method again. The transform can use this information
        // to avoid applying itself multiple times to the same class.
        if (redactedParameterIndices.isNotEmpty()) {
            visitAnnotation(REDACTED_MARKER_DESCRIPTOR, visible = true)
        }
    }
}
