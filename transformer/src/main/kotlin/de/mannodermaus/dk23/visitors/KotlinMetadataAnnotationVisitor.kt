package de.mannodermaus.dk23.visitors

import org.objectweb.asm.AnnotationVisitor

/**
 * A visitor implementation to read @Metadata information off of a Kotlin file.
 * The provided [onResult] lambda will provide the parsed annotation,
 * or null if it couldn't be read successfully.
 */
internal class KotlinMetadataAnnotationVisitor(
    api: Int,
    next: AnnotationVisitor,
    private val onResult: (Metadata?) -> Unit,
) : AnnotationVisitor(api, next) {

    private var metadataVersion: IntArray? = null
    private var kind: Int? = null
    private var data1: Array<String>? = null
    private var data2: Array<String>? = null

    override fun visit(name: String?, value: Any?) {
        super.visit(name, value)

        when (name) {
            "mv" -> metadataVersion = value as? IntArray
            "k" -> kind = value as? Int
            else -> {
                /* No-op */
            }
        }
    }

    override fun visitArray(name: String?): AnnotationVisitor {
        val next = super.visitArray(name)

        return when (name) {
            "d1" -> DataArrayAnnotationVisitor(api, next) { result ->
                data1 = result.toTypedArray()
            }

            "d2" -> DataArrayAnnotationVisitor(api, next) { result ->
                data2 = result.toTypedArray()
            }

            else -> next
        }
    }

    override fun visitEnd() {
        super.visitEnd()

        val mv = metadataVersion
        val k = kind
        val d1 = data1
        val d2 = data2
        val metadata = if (mv != null && k != null && d1 != null && d2 != null) {
            Metadata(
                metadataVersion = mv,
                kind = k,
                data1 = d1,
                data2 = d2,
            )
        } else {
            null
        }

        onResult(metadata)
    }

    /* Private */

    private class DataArrayAnnotationVisitor(
        api: Int,
        next: AnnotationVisitor,
        private val onResult: (List<String>) -> Unit,
    ) : AnnotationVisitor(api, next) {

        private val values = mutableListOf<String>()

        override fun visit(name: String?, value: Any?) {
            values.add(value.toString())
        }

        override fun visitEnd() {
            onResult(values)
        }
    }
}
